# Add Comment To Ticket

## Overview
Adds a comment to a ticket, optionally restricted to a project role.

## Request Details
- **Area**: Comments
- **Type**: CHANGE_SYSTEM
- **External API**: `POST /issue/{issueIdOrKey}/comment`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Key | Text | Yes | e.g. `KE-123`. |
| Comment Text | Paragraph | Yes | Converted to Atlassian Document Format. |
| Visibility Role | Text | No | Restricts visibility to this project role, e.g. `Administrators`. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Comment | Entity(Comment) | See [Entities → Comment](pages/entities.md). |
| Success | Boolean | |
| Message | Text | |

## Example
**Input**: Issue Key=`KE-123`, Comment Text=`Investigating now.`
**Output**: `Success=true`, `Message="Comment added to KE-123."`, `Comment={AuthorDisplayName: "Alice", Body: "Investigating now."}`

## Error Handling
- An unknown Visibility Role is rejected by Jira with the translated error message.
