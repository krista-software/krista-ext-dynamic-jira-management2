# Get Project Details

## Overview
Retrieves a single Jira project's details by key.

## Request Details
- **Area**: Projects
- **Type**: QUERY_SYSTEM
- **External API**: `GET /project/{projectIdOrKey}`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key | Text | Yes | The project's key, e.g. `KE`. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Project | Entity(Project) | See [Entities → Project](pages/entities.md). |

## Example
**Input**: Project Key=`KE`
**Output**: `Project={Key: "KE", Name: "Krista Extensions", ProjectTypeKey: "software", ...}`

## Error Handling
- **Not found** - an invalid or inaccessible project key returns a clear "not found" failure message.
