# Configuration

The extension is configured through the **Setup** tab in Krista. Configuration is stored
per-invoker - each invoker keeps its own independent connection and settings. Jira genuinely supports
two authentication mechanisms, so both are available side by side; pick one with **Authentication
Method**.

## Setup tab fields

| Field | Required | Default | Description |
|-------|----------|---------|-------------|
| Authentication Method | Yes | `ApiToken` | Choose `ApiToken` or `OAuth` - see [Authentication](pages/Authentication.md). |
| Account Email | Required for ApiToken | -- | The Atlassian account email that owns the API token. |
| API Token | Required for ApiToken | -- | Secured. An Atlassian API token (id.atlassian.com → Security → API tokens). |
| Jira Site URL | Required for ApiToken | -- | Your Jira Cloud site, e.g. `https://yourcompany.atlassian.net`. |
| OAuth Client ID | Required for OAuth | -- | The Client ID of an Atlassian OAuth 2.0 (3LO) app. |
| OAuth Client Secret | Required for OAuth | -- | Secured. The Client Secret of that OAuth app. |
| Default Assignee Account Id | No | -- | A Jira accountId used as the default Assignee on new tickets when none is given and the project defines no default. |

## Required vs optional

The extension validates these fields on save:

1. **Authentication Method** - must be present.
2. When **ApiToken**: **Account Email**, **API Token**, and **Jira Site URL** must all be non-empty.
3. When **OAuth**: **OAuth Client ID** and **OAuth Client Secret** must be non-empty - saving then
   opens the Jira sign-in popup to complete authorization.

## Notes

- Switching Authentication Method later re-validates the fields for the newly selected mode.
- Schemas discovered per project/issue type are cached for 10 minutes; use **Force Refresh** on
  [Discover Ticket Field Schema](pages/DiscoverTicketFieldSchema.md) if a project's fields just changed.
