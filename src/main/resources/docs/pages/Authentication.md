# Authentication

Dynamic Jira Management2 supports two authentication mechanisms - pick one on the Setup tab via
**Authentication Method**.

## Option A: API Token (recommended for a single Jira account)

Jira Cloud's token authentication is HTTP Basic over your account email and an API token (not a lone
bearer token), so both are required together.

1. Go to [id.atlassian.com → Security → API tokens](https://id.atlassian.com/manage-profile/security/api-tokens)
   and create a token.
2. Note your Jira Cloud site URL, e.g. `https://yourcompany.atlassian.net`.
3. On the Setup tab, set Authentication Method to `ApiToken`, then fill in Account Email, API Token,
   and Jira Site URL.

## Option B: OAuth 2.0 (3-legged, for a dedicated Jira app)

1. Create an OAuth 2.0 (3LO) app at [developer.atlassian.com](https://developer.atlassian.com/console/myapps/),
   add the Jira API with scopes `read:jira-work`, `write:jira-work`, `read:jira-user`, and
   `offline_access`.
2. Add the callback URL Krista shows after you select OAuth mode and save (it is derived from this
   appliance's own routing URL) to the app's allowed callback URLs.
3. On the Setup tab, set Authentication Method to `OAuth`, fill in OAuth Client ID and OAuth Client
   Secret, and save - Krista will open a Jira sign-in popup to complete authorization.

## Required scopes / permissions

| Scope / Permission | Why it's needed |
|---|---|
| `read:jira-work` | All read requests (projects, issue types, schema, tickets, comments, transitions, search). |
| `write:jira-work` | Create/Update Ticket, Add Comment, Transition Ticket Status, Assign/Unassign Ticket. |
| `read:jira-user` | Get Current User, Search Users, resolving "me" and assignee names. |
| `offline_access` | OAuth mode only - lets the extension silently refresh the access token instead of re-prompting. |

## Verifying

Run **Test Connection** in the Setup area. A successful result confirms the credentials and
connectivity; a failure returns an actionable message (e.g. missing credentials, 401 unauthorized, or
that Jira authorization is still required).
