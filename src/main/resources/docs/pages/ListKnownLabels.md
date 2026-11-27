# List Known Labels

## Overview
Retrieves labels already in use on this Jira instance, for auto-population/autocomplete on the
Labels field of Create and Update Ticket.

## Request Details
- **Area**: Reference Data
- **Type**: QUERY_SYSTEM
- **External API**: `GET /label`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Search Text | Text | No | Filters to labels containing this text (case-insensitive). |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Labels | [ Text ] | Matching label names. |

## Example
**Input**: Search Text=`urgent`
**Output**: `Labels=["urgent", "urgent-customer"]`

## Error Handling
- No matches returns an empty list, not an error.
