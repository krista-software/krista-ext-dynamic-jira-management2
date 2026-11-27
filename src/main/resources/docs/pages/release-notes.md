# Release Notes

- **Developer Name** : Krista Connector Agent
- **Krista Service APIs Java** : 1.0.126
- **Global Catalog Version** : Not Available

## v1.0.0 (November 2026)

Initial release: schema-driven Jira Cloud ticket management.

### New Features
* Dynamic project + issue-type schema discovery (Discover Ticket Field Schema, Get Editable Field
  Schema For Ticket), cached by Project Key + Issue Type with a refreshable 10-minute TTL.
* Dual authentication: API Token (email + Atlassian API token, HTTP Basic) and OAuth 2.0 (3-legged),
  selectable per connection.
* Full ticket lifecycle: create, retrieve, update, comment, workflow transitions (never a direct
  status update), and assignment/reassignment/unassignment with permission validation.
* Combined-filter JQL search (project, issue key, status, reporter, assignee, priority, issue type,
  labels, components, resolution, text, and date ranges), with "me" resolution, sorting, and
  token-based pagination plus an approximate result count.
* All 25 requests are callable by the Krista AI agent (MCP `tool = true`).
* Ticket entity search (KME) in addition to the dedicated Search Tickets request.

### Resolved Bugs
* Not Available

### Dependency Updates
* Not Available

### Known Issues
* "Total result count" on Search Tickets is approximate, not exact - a Jira Cloud platform
  constraint (the current `/search/jql` endpoint does not return an exact total; this extension uses
  Jira's own `/search/approximate-count` endpoint instead).

### Limitations and Caveats
* Requires Jira Cloud REST API v3 (not Jira Server/Data Center).
* OAuth mode requires the admin to register an Atlassian OAuth 2.0 (3LO) app and grant
  `read:jira-work`, `write:jira-work`, `read:jira-user`, and `offline_access`.
