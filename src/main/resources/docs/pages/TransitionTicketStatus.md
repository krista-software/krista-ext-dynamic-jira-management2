# Transition Ticket Status

## Overview
Moves a ticket to a new status via a named, currently-available workflow transition. The status
field is never set directly - only through a transition Jira's own workflow allows right now - and
the resulting status is read back and verified.

## Request Details
- **Area**: Workflow
- **Type**: CHANGE_SYSTEM
- **External API**: `POST /issue/{issueIdOrKey}/transitions`, followed by `GET /issue/{issueIdOrKey}` to verify
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Key | Text | Yes | e.g. `KE-123`. |
| Transition Name | Text | Yes | Must match one of [Get Available Workflow Transitions](pages/GetAvailableWorkflowTransitions.md)'s results. |
| Additional Fields | [ { FieldId: Text, Value: Text } ] | No | Any field the chosen transition requires (e.g. Resolution). |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Ticket | Entity(Ticket) | The ticket after the transition. |
| Success | Boolean | |
| Message | Text | |

## Example
**Input**: Issue Key=`KE-123`, Transition Name=`Done`, Additional Fields=`[{FieldId: "resolution", Value: "Fixed"}]`
**Output**: `Success=true`, `Message="KE-123 is now Done."`, `Ticket={..., StatusName: "Done"}`

## Error Handling
- **Unavailable transition** - fails with the transition name and the list of currently available
  transitions, before calling Jira.
- **Missing required field** - fails naming exactly which field the transition needs (e.g.
  Resolution) when it was not supplied.
