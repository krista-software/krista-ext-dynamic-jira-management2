# Create Ticket

## Overview
Creates a Jira ticket. Every mandatory and optional field is validated against the live schema
discovered for the chosen project and issue type before Jira is ever called - a missing mandatory
field fails with a clear message naming it, instead of an opaque Jira 400. Reporter is always the
authenticated user, never invented or user-supplied. A retried call with identical inputs within a
short window returns the original ticket instead of creating a duplicate.

## Request Details
- **Area**: Issues
- **Type**: CHANGE_SYSTEM
- **External API**: `POST /issue`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key | Text | Yes | The project's key. |
| Issue Type Id | Text | Yes | From [List Issue Types For Project](pages/ListIssueTypesForProject.md). |
| Summary | Text | Yes | Ticket title. |
| Description | Paragraph | No | Converted to Jira's Atlassian Document Format. |
| Priority | Text | No | A priority name or id; validated against the discovered schema's allowed values. |
| Assignee | Text | No | A display name, email, or accountId; resolved via user search. Falls back to the Setup tab's Default Assignee Account Id if omitted. |
| Labels | [ Text ] | No | Free-form labels. |
| Components | [ Text ] | No | Component names; resolved against [Get Project Components](pages/GetProjectComponents.md). |
| Due Date | Date | No | |
| Additional Fields | [ { FieldId: Text, Value: Text } ] | No | Any other field the discovered schema requires or allows (including custom fields), keyed by the real Jira field id. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Ticket | Entity(Ticket) | See [Entities → Ticket](pages/entities.md). |
| Success | Boolean | |
| Message | Text | The new ticket's key on success, or the specific validation/API failure. |

## Example
**Input**: Project Key=`KE`, Issue Type Id=`10000`, Summary=`Login fails on mobile`
**Output**: `Success=true`, `Message="Created KE-124."`, `Ticket={Key: "KE-124", Summary: "Login fails on mobile", StatusName: "To Do", ...}`

## Error Handling
- **Missing mandatory field** - `Success=false` with a message listing exactly which discovered
  field(s) were not supplied; no API call is made.
- **Invalid Priority/Assignee/Component value** - `Success=false` naming the invalid value and, for
  Priority, the valid options.
- **Ambiguous Assignee match** - `Success=false` asking for a more specific name when more than one
  user matches.
