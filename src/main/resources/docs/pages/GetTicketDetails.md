# Get Ticket Details

## Overview
Retrieves a ticket's full details by issue key or id.

## Request Details
- **Area**: Issues
- **Type**: QUERY_SYSTEM
- **External API**: `GET /issue/{issueIdOrKey}`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Key | Text | Yes | e.g. `KE-123`. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Ticket | Entity(Ticket) | See [Entities → Ticket](pages/entities.md). |

## Example
**Input**: Issue Key=`KE-123`
**Output**: `Ticket={Key: "KE-123", Summary: "Login fails on mobile", StatusName: "In Progress", ...}`

## Error Handling
- **Not found** - an invalid or inaccessible issue key returns a clear "not found" failure message.
