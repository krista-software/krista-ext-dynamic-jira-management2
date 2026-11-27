# Test Connection

## Overview
Validates that the configured credentials (either authentication mode) can reach Jira Cloud. Use
this after configuring or changing the Setup tab.

## Request Details
- **Area**: Setup
- **Type**: QUERY_SYSTEM
- **External API**: `GET /myself`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
None.

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Success | Boolean | True if Jira accepted the credentials. |
| Message | Text | "Connected to Jira." on success, or a specific failure reason. |

## Example
**Input**: (none)
**Output**: `Success=true`, `Message="Connected to Jira."`

## Error Handling
- **Invalid/expired credentials** - `Success=false` with the translated Jira error message (e.g.
  "Jira authentication failed - the configured credentials were rejected").
- **Jira authorization required (OAuth mode)** - `Success=false`, `Message="Jira authorization is
  required. Please connect your Jira account via the Setup tab."`
