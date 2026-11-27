# Get Available Workflow Transitions

## Overview
Retrieves the workflow transitions currently available for a ticket, including any field a
transition additionally requires (e.g. Resolution on a transition to Done). Use before
[Transition Ticket Status](pages/TransitionTicketStatus.md).

## Request Details
- **Area**: Workflow
- **Type**: QUERY_SYSTEM
- **External API**: `GET /issue/{issueIdOrKey}/transitions?expand=transitions.fields`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Key | Text | Yes | e.g. `KE-123`. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Transitions | [ Entity(Transition) ] | See [Entities → Transition](pages/entities.md). |

## Example
**Input**: Issue Key=`KE-123`
**Output**: `Transitions=[{Name: "Start Progress", ToStatusName: "In Progress"}, {Name: "Done", ToStatusName: "Done", RequiredFields: [{FieldId: "resolution", Name: "Resolution"}]}]`

## Error Handling
- The list reflects only transitions the current workflow state and the authenticated user's
  permissions actually allow - there is no error case; an empty list means no transitions are
  currently possible.
