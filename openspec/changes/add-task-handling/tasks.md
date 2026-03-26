## 1. API Client Task Methods

- [ ] 1.1 Add `getTask(id)` method to API client
- [ ] 1.2 Add `claimTask(id)` method to API client
- [ ] 1.3 Add `completeTask(id, variables)` method to API client
- [ ] 1.4 Add `delegateTask(id, username)` method to API client
- [ ] 1.5 Add TypeScript interfaces for task API requests/responses

## 2. Task Detail Page

- [ ] 2.1 Create `/tasks/[id]` page route with task metadata display
- [ ] 2.2 Add task metadata sidebar (name, assignee, priority, due date, process instance, creation time)
- [ ] 2.3 Add process/case instance link to navigate to instance detail
- [ ] 2.4 Add error state for non-existent task (404)
- [ ] 2.5 Add "Claim" button for unassigned tasks
- [ ] 2.6 Add "Complete" button for assigned tasks
- [ ] 2.7 Add "Delegate" button with user selection

## 3. Task Completion Form

- [ ] 3.1 Check for form schema by task definition key on task detail page
- [ ] 3.2 Render form using existing SurveyFormRenderer if schema exists
- [ ] 3.3 Show "no form required" message when no schema exists
- [ ] 3.4 Submit form variables on complete action
- [ ] 3.5 Handle completion success (redirect to instance detail)
- [ ] 3.6 Handle completion errors (show message, stay on page)

## 4. TaskCard Action Integration

- [ ] 4.1 Wire "Claim" button in TaskCard to `apiClient.claimTask(id)`
- [ ] 4.2 Wire "Complete" button in TaskCard to `apiClient.completeTask(id)`
- [ ] 4.3 Wire "View Details" button to navigate to `/tasks/[id]`
- [ ] 4.4 Add optimistic UI updates for claim/complete actions
- [ ] 4.5 Handle errors and revert optimistic updates

## 5. Instance Detail Task Actions

- [ ] 5.1 Add "Claim" button to unassigned tasks in instance detail page
- [ ] 5.2 Add "Complete" button to assigned tasks in instance detail page
- [ ] 5.3 Refresh instance detail data after task action
- [ ] 5.4 Show success/error feedback after task action

## 6. Testing

- [ ] 6.1 Test task detail page renders with valid task
- [ ] 6.2 Test task detail page shows error for invalid task ID
- [ ] 6.3 Test claim action from task card
- [ ] 6.4 Test complete action from task card
- [ ] 6.5 Test task detail page claim/complete actions
- [ ] 6.6 Test instance detail task actions
- [ ] 6.7 Test form rendering when schema exists
- [ ] 6.8 Test error handling for failed API calls
