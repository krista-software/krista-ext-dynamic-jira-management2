# List Accessible Projects

## Overview
Retrieves the Jira projects the authenticated user can access - the first step of the dynamic
schema-discovery workflow (pick a project here, then an issue type, then discover its schema).

## Request Details
- **Area**: Projects
- **Type**: QUERY_SYSTEM
- **External API**: `GET /project/search`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Search Text | Text | No | Filters projects by name/key. |
| Start At | Number | No | Pagination offset; defaults to 0. |
| Max Results | Number | No | Page size; defaults to 50. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Projects | [ Entity(Project) ] | See [Entities → Project](pages/entities.md). |
| Count | Number | Jira's reported total across all pages, not just this page's size. |

## Example
**Input**: Search Text=`Krista`
**Output**: `Projects=[{Key: "KE", Name: "Krista Extensions", ...}]`, `Count=1`

## Error Handling
- No matching projects returns an empty list with `Count=0`, not an error.
