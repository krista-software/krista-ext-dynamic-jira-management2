# Update Ticket Fields

## Overview
Updates only the fields explicitly supplied - every field left blank here is left completely
untouched on the ticket, never overwritten with a blank value. Validated against the ticket's own
live editable-field schema.

## Request Details
- **Area**: Issues
- **Type**: CHANGE_SYSTEM
- **External API**: `PUT /issue/{issueIdOrKey}`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Key | Text | Yes | e.g. `KE-123`. |
| Summary | Text | No | |
| Description | Paragraph | No | Converted to Atlassian Document Format. |
| Priority | Text | No | Validated against the discovered schema's allowed values. |
| Labels | [ Text ] | No | Replaces the full label set. |
| Components | [ Text ] | No | Component names; resolved against the project's components. |
| Due Date | Date | No | |
| Additional Fields | [ { FieldId: Text, Value: Text } ] | No | Any other editable field, keyed by its real Jira field id - see [Get Editable Field Schema For Ticket](pages/GetEditableFieldSchemaForTicket.md). |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Ticket | Entity(Ticket) | The ticket after the update. |
| Success | Boolean | |
| Message | Text | |

## Example
**Input**: Issue Key=`KE-123`, Priority=`High`
**Output**: `Success=true`, `Message="Updated KE-123."`, `Ticket={..., PriorityName: "High"}`

## Error Handling
- **No fields supplied** - fails with "No fields were supplied to update."
- **Field not editable** - fails naming any field id that isn't in the ticket's current editable
  schema, before calling Jira.
- **Invalid value** - fails naming the field and its valid options.
