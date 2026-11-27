# Health Check

## Overview
Reports the extension's JVM memory health alongside a lightweight Jira connectivity ping.

## Request Details
- **Area**: Setup
- **Type**: CHANGE_SYSTEM
- **External API**: `GET /myself` (liveness ping)
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
None.

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Is Healthy | Boolean | False if the Jira ping failed. |
| Heap Used MB | Number | Current JVM heap usage. |
| Details | Text | "OK" or the failure reason. |

## Example
**Input**: (none)
**Output**: `Is Healthy=true`, `Heap Used MB=128.0`, `Details="OK"`

## Error Handling
- A failed Jira ping sets `Is Healthy=false` with the translated error in `Details`; the JVM heap
  figures are still reported regardless.
