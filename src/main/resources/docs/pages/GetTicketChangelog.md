# Get Ticket Changelog

## Overview
Retrieves a ticket's paginated field-change history.

## Request Details
- **Area**: History
- **Type**: QUERY_SYSTEM
- **External API**: `GET /issue/{issueIdOrKey}/changelog`
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
| Changes | [ Entity(ChangelogEntry) ] | See [Entities → ChangelogEntry](pages/entities.md). |
| Count | Number | Total history entries. |

## Example
**Input**: Issue Key=`KE-123`
**Output**: `Changes=[{AuthorDisplayName: "Alice", Changes: [{Field: "status", FromValue: "To Do", ToValue: "In Progress"}]}]`, `Count=4`

## Error Handling
- A ticket with no history returns an empty list with `Count=0`, not an error.
