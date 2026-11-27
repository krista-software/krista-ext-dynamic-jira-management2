# Implementation Plan — Dynamic Jira Management2

## Extension summary

| | |
|---|---|
| Title | Dynamic Jira Management2 |
| Name (module slug) | `dynamic-jira-management2` |
| Version | 1.0.0 |
| Package | `app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2` |
| Ecosystem | ZZZ Test Sandbox |
| Domain | ZZZ Delete Me |
| Provider | Jira Cloud (Atlassian), REST API v3 |

A schema-driven Jira Cloud integration. Every project and issue type can require different
fields, so instead of one fixed "create ticket" form, the extension looks up each project's and
issue type's actual field rules from Jira at the moment of use (which fields are mandatory, what
values are allowed, what defaults apply — including custom fields) and uses that to validate and
pre-fill requests, asking the user only for whatever mandatory information is still missing.

**Architecture note on "dynamic catalog generation":** Krista catalog requests/entities are
Java classes compiled into the extension ahead of time — there is no framework mechanism to
generate a brand-new catalog form per Jira project at runtime. The requests below implement the
closest supported equivalent: a fixed set of well-known typed fields (Project, Issue Type,
Summary, Description, Priority, Assignee, Labels, Components, Due Date) plus an `AdditionalFields`
key/value list that carries whatever project-specific or custom fields the live schema lookup
says are needed, validated server-side against that lookup before any Jira API call is made. The
**Discover Ticket Field Schema** and **Get Editable Field Schema For Ticket** requests exist
specifically so a conversation/agent can see those live rules and decide what to ask for.

## Authentication

**User's confirmed choice:** `api-key, oauth2-interactive` — Jira genuinely supports both, so
both are implemented as admin-selectable options on the Setup tab, following the same pattern the
`servicenow` extension already uses (`@Field.PickOne(name="AuthType", values={"ApiToken","OAuth"})`
— see `authentication-patterns.md`, Pattern C). **Primary pattern for the build: `A`** (API Token
is listed first in the user's answer); OAuth is the secondary, fully-implemented option, not a
placeholder.

| Option | Krista pattern | How it works |
|---|---|---|
| **API Token** (selected as primary) | A | Jira Cloud's token auth is HTTP Basic over `email:apiToken`, not a lone bearer token — so this option has **two** connection fields: `Email` (required) and `API Token` (required, secured). The HTTP client sends `Authorization: Basic base64(email:apiToken)` directly to the tenant's own site, e.g. `https://<your-domain>.atlassian.net/rest/api/3`. |
| **OAuth 2.0 (interactive)** | C | Standard 3-legged flow: browser consent at `https://auth.atlassian.com/authorize` → code exchange at `https://auth.atlassian.com/oauth/token` (with `offline_access` for a refresh token) → `GET https://api.atlassian.com/oauth/token/accessible-resources` resolves the site's `cloudId` → all calls go through `https://api.atlassian.com/ex/jira/{cloudId}/rest/api/3`. Uses the standard `RequestAuthenticator` + `AuthCallbackResource` + `AttributeStore` + scribejava templates (ported from the Gmail reference, per the skill). |

Both options resolve and cache the authenticated identity via `GET /myself` (used for Test
Connection, Reporter defaulting, and resolving "me" in search/assignment).

**Note:** because the two auth options hit different base URLs (tenant-direct vs.
`api.atlassian.com` proxy), the HTTP client selects the base URL at call time from which `AuthType`
is configured for that connection.

## Areas and catalog requests

### Setup
| Request | Inputs | Output | External endpoint | Reuse |
|---|---|---|---|---|
| Test Connection | — | Success, Message | `GET /myself` | new |
| Health Check | — | IsHealthy, HeapUsedMb, Details | `GET /myself` (liveness) | new |

### Projects & Metadata
| Request | Inputs | Output | External endpoint | Reuse |
|---|---|---|---|---|
| List Accessible Projects | SearchText, StartAt, MaxResults | `[ Entity(Project) ]` + Count | `GET /project/search` | new |
| Get Project Details | ProjectKey | `Entity(Project)` | `GET /project/{key}` | new |
| List Issue Types For Project | ProjectKey | `[ Entity(IssueType) ]` | `GET /issue/createmeta/{key}/issuetypes` | new |
| List Priorities | — | `[ Entity(Priority) ]` | `GET /priority/search` | new |
| List Statuses | ProjectKey?, IssueTypeId? | `[ Entity(Status) ]` | `GET /project/{key}/statuses` or `GET /status` | new |
| List Resolutions | — | `[ Entity(Resolution) ]` | `GET /resolution/search` | new |
| Get Project Components | ProjectKey | `[ { Id, Name } ]` | `GET /project/{key}/components` | new |
| List Known Labels | SearchText? | `[ Text ]` | `GET /label` | new |

### Schema Discovery (highest priority per the requirement)
| Request | Inputs | Output | External endpoint | Reuse |
|---|---|---|---|---|
| Discover Ticket Field Schema | ProjectKey, IssueTypeId | `[ Entity(FieldSchema) ]` | `GET /issue/createmeta/{key}/issuetypes/{id}` (+ `GET /field`, `GET /field/{id}/context/{ctx}/option` internally) | new |
| Get Editable Field Schema For Ticket | IssueKey | `[ Entity(FieldSchema) ]` | `GET /issue/{key}/editmeta` | new |

Schema results are **cached by Project Key + Issue Type** with a TTL and are refreshed on demand
or when a create/update call fails validation against a stale cached schema.

### Users
| Request | Inputs | Output | External endpoint | Reuse |
|---|---|---|---|---|
| Get Current User | — | `Entity(User)` | `GET /myself` | new |
| Search Users | SearchText?, ProjectKey?, MaxResults? | `[ Entity(User) ]` | `GET /user/assignable/search` or `GET /user/search` | new |

### Tickets
| Request | Inputs | Output | External endpoint | Reuse |
|---|---|---|---|---|
| Create Ticket | ProjectKey, IssueTypeId, Summary, Description?, Priority?, Assignee?, Labels?, Components?, DueDate?, AdditionalFields? | `Entity(Ticket)` + Success + Message | `POST /issue` | new |
| Get Ticket Details | IssueKey | `Entity(Ticket)` | `GET /issue/{key}` | new |
| Update Ticket Fields | IssueKey, (any subset of the above) | `Entity(Ticket)` + Success + Message | `PUT /issue/{key}` | new |
| Get Ticket Changelog | IssueKey, StartAt?, MaxResults? | `[ Entity(ChangelogEntry) ]` + Count | `GET /issue/{key}/changelog` | new |

### Comments
| Request | Inputs | Output | External endpoint | Reuse |
|---|---|---|---|---|
| Add Comment To Ticket | IssueKey, CommentText, VisibilityRole? | `Entity(Comment)` + Success + Message | `POST /issue/{key}/comment` | new |
| Get Ticket Comments | IssueKey, StartAt?, MaxResults? | `[ Entity(Comment) ]` + Count | `GET /issue/{key}/comment` | new |

### Workflow
| Request | Inputs | Output | External endpoint | Reuse |
|---|---|---|---|---|
| Get Available Workflow Transitions | IssueKey | `[ Entity(Transition) ]` | `GET /issue/{key}/transitions?expand=transitions.fields` | new |
| Transition Ticket Status | IssueKey, TransitionName, AdditionalFields? | `Entity(Ticket)` + Success + Message | `POST /issue/{key}/transitions` + verify via `GET /issue/{key}` | new |

The status field is **never** set directly — only via a validated transition, and the resulting
status is read back and confirmed.

### Assignment
| Request | Inputs | Output | External endpoint | Reuse |
|---|---|---|---|---|
| Assign Or Reassign Ticket | IssueKey, AssignToCurrentUser?, AssigneeSearchText? | `Entity(Ticket)` + Success + Message | `PUT /issue/{key}/assignee` (+ `GET /mypermissions` check) | new |
| Unassign Ticket | IssueKey | Success + Message | `PUT /issue/{key}/assignee` (accountId=null) | new |

### Search
| Request | Inputs | Output | External endpoint | Reuse |
|---|---|---|---|---|
| Search Tickets | ProjectKey?, IssueKey?, Status?, Reporter?, Assignee?, Priority?, IssueType?, Labels?, Components?, Resolution?, TextSearch?, Created/Updated/Due date ranges?, SortBy?, SortDescending?, MaxResults?, NextPageToken? | `[ Entity(Ticket) ]` + ApproxCount + NextPageToken + IsLast | `POST /search/jql` + `POST /search/approximate-count` | new |

**Endpoint-compatibility note:** Jira deprecated the old `GET/POST /rest/api/3/search` endpoint in
favor of the token-paginated `POST /search/jql`, which does not return an exact `total`. This plan
uses the current, supported endpoint and gets a result count via the companion
`POST /search/approximate-count` call — so "total result counts" is delivered as an approximate
count rather than an exact one, which is a Jira platform constraint, not an implementation gap.

## Entities

`Project`, `IssueType`, `FieldSchema`, `Priority`, `Status`, `Resolution`, `User`, `Ticket`,
`Comment`, `Transition`, `ChangelogEntry` — 11 entities, one per distinct domain object returned.
Nested objects (e.g. a Ticket's assignee/status/components) are modeled as inline composite
fields (`{ Key: Type, ... }` / `[ { ... } ]`), never as nested `@Entity` references, per the
framework's entity-nesting rule.

Names are deliberately vendor-agnostic (`Ticket`, not `Jira Ticket`; `User`, not `Jira User`) so a
future extension placed in the same domain can reuse them rather than re-inventing equivalents —
see Domain reuse below.

## Capabilities

| Capability | Enabled | Rationale |
|---|---|---|
| Entity Search | ✅ | The `Ticket` entity is made searchable (`supportStore=true`, JQL dialect) via the entity-search-implementation skill, so Krista's own query engine can resolve/compose Tickets generically — complementing, not duplicating, the dedicated Search Tickets request, which stays the natural-language-filter entry point. |
| MCP (`tool=true`) | ✅ | Required `krista-apis` ≥ 1.0.125-sp1. Set on every user-facing request (all areas above except raw internal helper calls) so an AI agent can drive the full natural-language ticket-management flow described in the requirement. |
| Webhooks | ❌ | Not requested — no "wait for event" capability was asked for. |
| Solutions tab | ❌ | Not requested. |
| Files | ❌ | Not requested — no attachment upload/download capability was asked for. |

## Domain reuse

`domain-catalog.json` returned `"found": false` for **ZZZ Test Sandbox / ZZZ Delete Me** — this
ecosystem/domain has never been published, so there is nothing existing to reuse, duplicate, or
conflict with. Every request and entity in this plan is **new**. Per the domain-reuse guidance,
names were still written in generic, vendor-agnostic style (`Ticket`, `Project`, `User`,
`Comment`, `Status`, `Priority`, …) rather than `Jira Ticket`/`Jira User`/etc., so that if another
extension is ever placed in this same domain, it has consistent, reusable names to match against.

## Placeholder IDs — must replace before deploy

- `@Domain` `id` / `ecosystemId` / `ecosystemName` / `ecosystemVersion` — **ZZZ Test Sandbox /
  ZZZ Delete Me** is not yet a registered domain in Krista Studio (`domain-catalog.json`
  `found: false`), so the build will mint stable placeholder UUIDs for these. They compile fine
  but **will not bind** to the real platform catalog until replaced with the actual registered
  Domain/Ecosystem IDs (or until this domain is genuinely registered).
- No other placeholders are anticipated (auth uses real, user-provided credentials; no other
  shared/registered identifiers are involved).

## Endpoint coverage (completeness check)

All 29 endpoints inventoried in `api-model.json` are exercised: 25 are the primary call for a
listed catalog request; the remainder (`GET /field`, `GET /field/search`, `GET /field/{id}/context`,
`GET /field/{id}/context/{ctx}/option`, `GET /user`, `GET /mypermissions`,
`GET https://api.atlassian.com/oauth/token/accessible-resources`) are internal supporting calls
used by schema discovery, user hydration, permission validation, and the OAuth flow respectively —
documented inline above rather than exposed as their own catalog requests, since they aren't
independently named capabilities in the requirement.
