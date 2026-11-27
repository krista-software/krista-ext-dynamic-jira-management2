# List Statuses

## Overview
Retrieves workflow statuses - scoped to a project's (and optionally issue type's) own workflow when
Project Key is given, otherwise every status defined on the instance.

## Request Details
- **Area**: Reference Data
- **Type**: QUERY_SYSTEM
- **External API**: `GET /project/{projectIdOrKey}/statuses` (scoped) or `GET /status` (global)
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key | Text | No | Scopes to this project's actual workflow statuses. |
| Issue Type Id | Text | No | Further scopes to this issue type's workflow, when Project Key is given. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Statuses | [ Entity(Status) ] | See [Entities → Status](pages/entities.md). |

## Example
**Input**: Project Key=`KE`
**Output**: `Statuses=[{Name: "To Do", CategoryKey: "new"}, {Name: "In Progress", CategoryKey: "indeterminate"}, {Name: "Done", CategoryKey: "done"}]`

## Error Handling
- None specific to this request.
