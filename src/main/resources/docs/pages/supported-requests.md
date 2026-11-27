# Supported Requests

Dynamic Jira Management2 exposes **25 catalog requests** across **11 areas**. Each links to its
detailed page. All 25 are callable by the Krista AI agent (MCP `tool = true`).

## Setup
| Request | Type | Description |
|---|---|---|
| [Test Connection](pages/TestConnection.md) | QUERY_SYSTEM | Validate connectivity and authentication. |
| [Health Check](pages/HealthCheck.md) | CHANGE_SYSTEM | Report JVM health and a lightweight Jira ping. |

## Projects
| Request | Type | Description |
|---|---|---|
| [List Accessible Projects](pages/ListAccessibleProjects.md) | QUERY_SYSTEM | Retrieve projects the authenticated user can access. |
| [Get Project Details](pages/GetProjectDetails.md) | QUERY_SYSTEM | Retrieve one project by key. |
| [Get Project Components](pages/GetProjectComponents.md) | QUERY_SYSTEM | Retrieve a project's configured components. |

## Reference Data
| Request | Type | Description |
|---|---|---|
| [List Priorities](pages/ListPriorities.md) | QUERY_SYSTEM | Retrieve configured priority levels. |
| [List Statuses](pages/ListStatuses.md) | QUERY_SYSTEM | Retrieve workflow statuses. |
| [List Resolutions](pages/ListResolutions.md) | QUERY_SYSTEM | Retrieve configured resolutions. |
| [List Known Labels](pages/ListKnownLabels.md) | QUERY_SYSTEM | Retrieve labels already in use. |

## Issue Types & Schema
| Request | Type | Description |
|---|---|---|
| [List Issue Types For Project](pages/ListIssueTypesForProject.md) | QUERY_SYSTEM | Retrieve a project's issue types. |
| [Discover Ticket Field Schema](pages/DiscoverTicketFieldSchema.md) | QUERY_SYSTEM | Discover create-ticket field rules for a project + issue type. |
| [Get Editable Field Schema For Ticket](pages/GetEditableFieldSchemaForTicket.md) | QUERY_SYSTEM | Discover which fields are editable on an existing ticket. |

## Current User
| Request | Type | Description |
|---|---|---|
| [Get Current User](pages/GetCurrentUser.md) | QUERY_SYSTEM | Retrieve the authenticated Jira user. |

## Issues
| Request | Type | Description |
|---|---|---|
| [Create Ticket](pages/CreateTicket.md) | CHANGE_SYSTEM | Create a ticket, schema-validated. |
| [Get Ticket Details](pages/GetTicketDetails.md) | QUERY_SYSTEM | Retrieve a ticket's full details. |
| [Update Ticket Fields](pages/UpdateTicketFields.md) | CHANGE_SYSTEM | Update only the fields supplied. |

## History
| Request | Type | Description |
|---|---|---|
| [Get Ticket Changelog](pages/GetTicketChangelog.md) | QUERY_SYSTEM | Retrieve a ticket's field-change history. |

## Comments
| Request | Type | Description |
|---|---|---|
| [Add Comment To Ticket](pages/AddCommentToTicket.md) | CHANGE_SYSTEM | Add a comment. |
| [Get Ticket Comments](pages/GetTicketComments.md) | QUERY_SYSTEM | Retrieve comments, paginated. |

## Workflow
| Request | Type | Description |
|---|---|---|
| [Get Available Workflow Transitions](pages/GetAvailableWorkflowTransitions.md) | QUERY_SYSTEM | Retrieve transitions currently available. |
| [Transition Ticket Status](pages/TransitionTicketStatus.md) | CHANGE_SYSTEM | Move a ticket through a named, available transition. |

## Assignment
| Request | Type | Description |
|---|---|---|
| [Search Users](pages/SearchUsers.md) | QUERY_SYSTEM | Find candidate assignees. |
| [Assign Or Reassign Ticket](pages/AssignOrReassignTicket.md) | CHANGE_SYSTEM | Assign to the current user or a matched user. |
| [Unassign Ticket](pages/UnassignTicket.md) | CHANGE_SYSTEM | Remove the assignee. |

## Search
| Request | Type | Description |
|---|---|---|
| [Search Tickets](pages/SearchTickets.md) | QUERY_SYSTEM | Combined-filter JQL search with sorting and pagination. |
