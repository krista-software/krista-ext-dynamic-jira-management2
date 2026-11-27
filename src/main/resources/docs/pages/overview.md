# Overview

Dynamic Jira Management2 connects Krista to **Jira Cloud** via its REST API v3. Use it to create,
view, update, comment on, transition, assign, and search Jira tickets directly from Krista workflows,
conversations, and AI agents - described in natural language rather than filled out field-by-field.

## Use cases

- **Dynamic project schema discovery** - select a project and issue type, and see exactly which
  fields Jira requires, their allowed values and defaults, before creating a ticket. No project is
  hardcoded; every project and issue type can have its own mandatory/optional fields, including
  custom fields, and the extension adapts automatically.
- **Full ticket lifecycle** - create, retrieve, and update tickets; add and read comments; move a
  ticket through its allowed workflow transitions (never by force-setting the status field); assign,
  reassign, or unassign tickets.
- **Advanced search** - combined filters across project, status, reporter, assignee, priority, issue
  type, labels, components, dates, resolution, and free text, with sorting and pagination. "me"
  resolves to the authenticated Jira identity.

## What you can do

The extension exposes 25 requests grouped by area - see
[Supported Requests](pages/supported-requests.md) for the full list, or the sidebar for each
request's detailed page.

## Before you start

1. Obtain credentials - see [Authentication](pages/Authentication.md).
2. Configure the Setup tab - see [Configuration](pages/ExtensionConfiguration.md).
3. Run **Test Connection** (Setup area) to verify connectivity.
