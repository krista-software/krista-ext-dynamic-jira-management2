# List Resolutions

## Overview
Retrieves the resolution values configured on this Jira instance, for use as a Search Tickets filter
or a workflow-transition field value.

## Request Details
- **Area**: Reference Data
- **Type**: QUERY_SYSTEM
- **External API**: `GET /resolution/search`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
None.

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Resolutions | [ Entity(Resolution) ] | See [Entities → Resolution](pages/entities.md). |

## Example
**Input**: (none)
**Output**: `Resolutions=[{Name: "Fixed"}, {Name: "Won't Fix"}, {Name: "Duplicate"}]`

## Error Handling
- None specific to this request.
