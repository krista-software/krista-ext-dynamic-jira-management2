# Search Tickets

## Overview
Advanced, JQL-backed ticket search across any combination of project, issue key, status, reporter,
assignee, priority, issue type, labels, components, resolution, free text, and created/updated/due
date ranges - with sorting and pagination. Supports natural-language-shaped requests such as "tickets
assigned to me", "unresolved bugs in project KE", "high-priority tickets assigned to me", and
"tickets created in the last seven days" (by passing a Created After date one week back).

## Request Details
- **Area**: Search
- **Type**: QUERY_SYSTEM
- **External API**: `POST /search/jql` (token-paginated) plus `POST /search/approximate-count`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key | Text | No | |
| Issue Key | Text | No | |
| Status | Text | No | |
| Reporter | Text | No | An accountId, or `me` for the authenticated user. |
| Assignee | Text | No | An accountId, or `me` for the authenticated user. |
| Priority | Text | No | |
| Issue Type | Text | No | |
| Labels | [ Text ] | No | Matches any of the given labels. |
| Components | [ Text ] | No | Matches any of the given components. |
| Resolution | Text | No | |
| Text Search | Text | No | Free-text search across summary/description/comments. |
| Created After / Created Before | Date | No | |
| Updated After / Updated Before | Date | No | |
| Due After / Due Before | Date | No | |
| Sort By | Text | No | A Ticket field name, e.g. `Updated`. |
| Sort Descending | Boolean | No | |
| Max Results | Number | No | Defaults to 50. |
| Next Page Token | Text | No | From a previous Search Tickets call's output, to fetch the next page. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Tickets | [ Entity(Ticket) ] | See [Entities → Ticket](pages/entities.md). |
| Approx Count | Number | An approximate total match count (Jira's current search API does not return an exact total - see Known Issues in [Release Notes](pages/release-notes.md)). |
| Next Page Token | Text | Pass back in to fetch the next page; absent on the last page. |
| Is Last | Boolean | True when there are no more pages. |

## Example
**Input**: Assignee=`me`, Status=`In Progress`
**Output**: `Tickets=[{Key: "KE-123", Summary: "Login fails on mobile", ...}]`, `Approx Count=3.0`, `Is Last=true`

## Error Handling
- An invalid filter value is surfaced as Jira's own JQL validation error.
- If the approximate-count lookup itself fails, `Approx Count` is simply omitted - the ticket results
  are still returned.
