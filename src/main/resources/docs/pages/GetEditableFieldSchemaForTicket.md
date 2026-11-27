# Get Editable Field Schema For Ticket

## Overview
Discovers which fields on an already-existing ticket are editable right now (given its current
screen configuration and workflow state), and their rules - drives
[Update Ticket Fields](pages/UpdateTicketFields.md)'s validation.

## Request Details
- **Area**: Issue Types & Schema
- **Type**: QUERY_SYSTEM
- **External API**: `GET /issue/{issueIdOrKey}/editmeta`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Key | Text | Yes | e.g. `KE-123`. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Fields | [ Entity(FieldSchema) ] | See [Entities → FieldSchema](pages/entities.md). |

## Example
**Input**: Issue Key=`KE-123`
**Output**: `Fields=[{FieldId: "summary", Required: true}, {FieldId: "priority", Required: false, AllowedValues: [...]}, ...]`

## Error Handling
- A field not present in this result is rejected by Update Ticket Fields before any Jira call is
  made, with a message naming the unsupported field.
