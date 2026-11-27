# Get Current User

## Overview
Retrieves the authenticated Jira user - used throughout the extension to default Reporter and to
resolve "me" in Search Tickets and Assign Or Reassign Ticket.

## Request Details
- **Area**: Current User
- **Type**: QUERY_SYSTEM
- **External API**: `GET /myself`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
None.

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| User | Entity(User) | See [Entities → User](pages/entities.md). |

## Example
**Input**: (none)
**Output**: `User={AccountId: "abc123", DisplayName: "Alice Reporter", Active: true}`

## Error Handling
- None specific to this request - failures surface as the standard authentication error.
