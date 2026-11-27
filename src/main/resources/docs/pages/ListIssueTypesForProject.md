# List Issue Types For Project

## Overview
Retrieves the issue types available within a project (e.g. Bug, Task, Story) - the second step of
the dynamic schema-discovery workflow.

## Request Details
- **Area**: Issue Types & Schema
- **Type**: QUERY_SYSTEM
- **External API**: `GET /issue/createmeta/{projectIdOrKey}/issuetypes`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key | Text | Yes | The project's key. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Issue Types | [ Entity(IssueType) ] | See [Entities → IssueType](pages/entities.md). Use an entry's Id with [Discover Ticket Field Schema](pages/DiscoverTicketFieldSchema.md). |

## Example
**Input**: Project Key=`KE`
**Output**: `Issue Types=[{Id: "10000", Name: "Bug"}, {Id: "10001", Name: "Story"}]`

## Error Handling
- A project with a single, simplified issue-type set (common on Team-managed projects) returns just
  that one issue type, not an error.
