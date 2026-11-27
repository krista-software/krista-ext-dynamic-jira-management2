# Assign Or Reassign Ticket

## Overview
Assigns a ticket to the current authenticated user, or to a user matched by name/email, with
permission validation.

## Request Details
- **Area**: Assignment
- **Type**: CHANGE_SYSTEM
- **External API**: `PUT /issue/{issueIdOrKey}/assignee` (preceded by user resolution and a
  permission check)
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Key | Text | Yes | e.g. `KE-123`. |
| Assign To Current User | Boolean | No | True to assign to "me" (the authenticated Jira identity). |
| Assignee Search Text | Text | No | A name/email to resolve to an assignee, when not assigning to the current user. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Ticket | Entity(Ticket) | The ticket after assignment. |
| Success | Boolean | |
| Message | Text | |

## Example
**Input**: Issue Key=`KE-123`, Assign To Current User=`true`
**Output**: `Success=true`, `Message="KE-123 is now assigned to Alice Reporter."`

## Error Handling
- **Ambiguous match** - fails listing every matching user's display name when Assignee Search Text
  matches more than one person.
- **Permission denied** - a clear permission error, not a generic failure, when the authenticated
  user lacks Assign Issues permission.
