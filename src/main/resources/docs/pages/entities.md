# Entities

Data structures returned by the requests. Request output tables link here instead of repeating the
full field list.

## Project
A Jira project that tickets belong to.

| Field | Type | Description |
|---|---|---|
| Id | Text | Primary key. |
| Key | Text | The project key, e.g. `KE`. |
| Name | Text | Display name. |
| Project Type Key | Text | e.g. `software`, `business`, `service_desk`. |
| Style | Text | `classic` or `next-gen` (team-managed). |
| Lead Display Name | Text | References User.Display Name. |

## IssueType
An issue type (e.g. Bug, Task, Story) available within a Project.

| Field | Type | Description |
|---|---|---|
| Id | Text | Primary key - pass as Issue Type Id to Discover Ticket Field Schema / Create Ticket. |
| Name | Text | e.g. `Bug`, `Task`, `Story`. |
| Description | Paragraph | Issue type description. |
| Is Subtask | Boolean | True for sub-task issue types. |

## FieldSchema
One field's live validation rules for a given project + issue type (or, from the edit variant, an
existing ticket) - the output of dynamic schema discovery.

| Field | Type | Description |
|---|---|---|
| Field Id | Text | The real Jira field id (e.g. `summary`, `customfield_10032`). |
| Name | Text | Human-readable field name. |
| Required | Boolean | True if Jira will reject the request without a value for this field. |
| Data Type | Text | e.g. `string`, `number`, `array`, `user`, `priority`, `date`. |
| Is Custom Field | Boolean | True for project/issue-type-specific custom fields. |
| Allowed Values | [ { Id, Value } ] | The fixed set of values Jira accepts, if any. |
| Default Value | Text | Jira's own default, if it has one. |
| Has Default Value | Boolean | True if Jira will fill this field in automatically when omitted. |

## Priority
A ticket priority level available on this Jira instance.

| Field | Type | Description |
|---|---|---|
| Id | Text | Primary key. |
| Name | Text | e.g. `Highest`, `High`, `Medium`, `Low`, `Lowest`, or an instance-specific custom priority. |

## Status
A workflow status a ticket can be in.

| Field | Type | Description |
|---|---|---|
| Id | Text | Primary key. |
| Name | Text | e.g. `To Do`, `In Progress`, `Done`. |
| Category Key | Text | `new`, `indeterminate`, or `done`. |

## Resolution
A resolution value usable as a Search Tickets filter or a workflow-transition field.

| Field | Type | Description |
|---|---|---|
| Id | Text | Primary key. |
| Name | Text | e.g. `Fixed`, `Won't Fix`, `Duplicate`. |

## User
A Jira user, identified by Atlassian accountId.

| Field | Type | Description |
|---|---|---|
| Account Id | Text | Primary key - the stable Jira Cloud user identifier. |
| Display Name | Text | Shown throughout the UI. |
| Email | Text | May be blank if the caller lacks permission to see it. |
| Active | Boolean | False for deactivated accounts. |

## Ticket
A Jira ticket (issue) - the central domain object of this extension.

| Field | Type | Description |
|---|---|---|
| Id | Text | Jira's internal numeric id. |
| Key | Text | Primary key, e.g. `KE-123`. |
| Summary | Text | Ticket title. |
| Description | Paragraph | Plain-text rendering of the ADF description. |
| Status Name | Text | References Status.Name. |
| Priority Name | Text | References Priority.Name. |
| Issue Type Name | Text | References IssueType.Name. |
| Project Key | Text | References Project.Key. |
| Reporter Display Name | Text | References User.Display Name. |
| Reporter Account Id | Text | References User.Account Id. |
| Assignee Display Name | Text | References User.Display Name; blank when unassigned. |
| Assignee Account Id | Text | References User.Account Id; blank when unassigned. |
| Labels | [ Text ] | Free-form labels. |
| Components | [ { Id, Name } ] | Project components this ticket belongs to. |
| Due Date | Date | |
| Created | Date | |
| Updated | Date | |
| Resolution Name | Text | References Resolution.Name; blank while unresolved. |

## Comment
A comment on a Ticket.

| Field | Type | Description |
|---|---|---|
| Id | Text | Primary key. |
| Author Display Name | Text | References User.Display Name. |
| Body | Paragraph | Plain-text rendering of the ADF comment body. |
| Created | Date | |
| Updated | Date | |

## Transition
An available workflow transition for a specific Ticket - the only legal way to change its status.

| Field | Type | Description |
|---|---|---|
| Id | Text | Primary key - pass as the transition's internal id when executing it. |
| Name | Text | The transition's display name, e.g. `Start Progress`, `Done`. |
| To Status Name | Text | The status the ticket will have after this transition. |
| Required Fields | [ { FieldId, Name } ] | Any field this transition additionally requires (e.g. Resolution). |

## ChangelogEntry
One historical field-change event on a Ticket.

| Field | Type | Description |
|---|---|---|
| Id | Text | Primary key. |
| Author Display Name | Text | References User.Display Name. |
| Created | Date | |
| Changes | [ { Field, FromValue, ToValue } ] | Each field that changed in this event. |
