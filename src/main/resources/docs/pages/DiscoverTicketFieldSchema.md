# Discover Ticket Field Schema

## Overview
The core of this extension's dynamic schema discovery: returns every field Jira will ask for when
creating a ticket of this issue type in this project - which are mandatory, their data type, their
allowed values, and their defaults, including project-specific custom fields. Use this before Create
Ticket so no mandatory field is ever invented or missed.

## Request Details
- **Area**: Issue Types & Schema
- **Type**: QUERY_SYSTEM
- **External API**: `GET /issue/createmeta/{projectIdOrKey}/issuetypes/{issueTypeId}` (plus internal
  `GET /field` and `GET /field/{fieldId}/context/{contextId}/option` lookups for richer custom-field
  metadata)
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key | Text | Yes | The project's key. |
| Issue Type Id | Text | Yes | From [List Issue Types For Project](pages/ListIssueTypesForProject.md). |
| Force Refresh | Boolean | No | Bypasses the cached schema (cached 10 minutes by Project Key + Issue Type). |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Fields | [ Entity(FieldSchema) ] | See [Entities → FieldSchema](pages/entities.md). |

## Example
**Input**: Project Key=`KE`, Issue Type Id=`10000`
**Output**: `Fields=[{FieldId: "summary", Required: true, DataType: "string"}, {FieldId: "customfield_10032", Name: "Severity", Required: true, AllowedValues: [{Id:"1",Value:"Critical"}, ...]}, ...]`

## Error Handling
- Results are cached by Project Key + Issue Type for 10 minutes; a transient fetch error is never
  cached, so the next call retries against Jira rather than repeating a stale failure.
