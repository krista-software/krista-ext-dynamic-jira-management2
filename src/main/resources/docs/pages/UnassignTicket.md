# Unassign Ticket

## Overview
Removes the assignee from a ticket.

## Request Details
- **Area**: Assignment
- **Type**: CHANGE_SYSTEM
- **External API**: `PUT /issue/{issueIdOrKey}/assignee` with `accountId=null`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Key | Text | Yes | e.g. `KE-123`. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Success | Boolean | |
| Message | Text | |

## Example
**Input**: Issue Key=`KE-123`
**Output**: `Success=true`, `Message="KE-123 is now unassigned."`

## Error Handling
- **Permission denied** - a clear permission error when the authenticated user lacks Assign Issues
  permission.
