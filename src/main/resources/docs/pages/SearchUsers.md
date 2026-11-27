# Search Users

## Overview
Finds candidate users, preferring assignment-eligible users when a project is given - the lookup
behind name/email matching for Assignee fields.

## Request Details
- **Area**: Assignment
- **Type**: QUERY_SYSTEM
- **External API**: `GET /user/assignable/search` (with Project Key) or `GET /user/search` (without)
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Search Text | Text | No | Name/email fragment to match. |
| Project Key | Text | No | Restricts results to users assignable on this project. |
| Max Results | Number | No | Defaults to 50. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Users | [ Entity(User) ] | See [Entities → User](pages/entities.md). |

## Example
**Input**: Search Text=`alice`, Project Key=`KE`
**Output**: `Users=[{AccountId: "abc123", DisplayName: "Alice Reporter"}]`

## Error Handling
- No matches returns an empty list, not an error.
