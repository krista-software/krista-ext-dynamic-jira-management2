# Get Ticket Comments

## Overview
Retrieves a ticket's comments, paginated.

## Request Details
- **Area**: Comments
- **Type**: QUERY_SYSTEM
- **External API**: `GET /issue/{issueIdOrKey}/comment`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Key | Text | Yes | e.g. `KE-123`. |
| Start At | Number | No | Pagination offset; defaults to 0. |
| Max Results | Number | No | Page size; defaults to 50. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Comments | [ Entity(Comment) ] | See [Entities → Comment](pages/entities.md). |
| Count | Number | Total comments on the ticket. |

## Example
**Input**: Issue Key=`KE-123`
**Output**: `Comments=[{AuthorDisplayName: "Alice", Body: "Investigating now."}]`, `Count=1`

## Error Handling
- A ticket with no comments returns an empty list with `Count=0`, not an error.
