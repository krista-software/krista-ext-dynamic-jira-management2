# Get Project Components

## Overview
Retrieves the components configured on a project, for populating or validating the Components field
on Create/Update Ticket.

## Request Details
- **Area**: Projects
- **Type**: QUERY_SYSTEM
- **External API**: `GET /project/{projectIdOrKey}/components`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key | Text | Yes | The project's key. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Components | [ { Id: Text, Name: Text } ] | The project's configured components. |

## Example
**Input**: Project Key=`KE`
**Output**: `Components=[{Id: "100", Name: "Mobile App"}, {Id: "101", Name: "Backend"}]`

## Error Handling
- A project with no components configured returns an empty list, not an error.
