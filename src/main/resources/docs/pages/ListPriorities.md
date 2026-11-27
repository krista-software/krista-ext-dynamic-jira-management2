# List Priorities

## Overview
Retrieves the priority levels configured on this Jira instance, for populating or validating the
Priority field.

## Request Details
- **Area**: Reference Data
- **Type**: QUERY_SYSTEM
- **External API**: `GET /priority/search`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
None.

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Priorities | [ Entity(Priority) ] | See [Entities → Priority](pages/entities.md). |

## Example
**Input**: (none)
**Output**: `Priorities=[{Name: "Highest"}, {Name: "High"}, {Name: "Medium"}, {Name: "Low"}, {Name: "Lowest"}]`

## Error Handling
- None specific to this request.
