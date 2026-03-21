# Form Schema Contract

**Version**: 1.0.0
**Purpose**: SurveyJS form schema JSON structure and process variable mapping contract
**Engine**: SurveyJS (React) with custom Flowable integration

---

## Form Schema JSON Structure

### Root Schema

```json
{
  "schemaVersion": "1.0",
  "formId": "uuid-v4",
  "name": "Leave Request Form",
  "description": "Submit a leave request for manager approval",
  "version": "1.0.0",
  "processDefinitionKey": "leaveRequest",
  "taskDefinitionKey": "managerApproval",
  "tenantId": "tenant-uuid",
  "fields": [
    /* Field definitions */
  ],
  "validation": {
    /* Global validation rules */
  },
  "processVariableMapping": {
    /* Field to variable mapping */
  },
  "permissions": {
    /* Field-level permissions per role */
  },
  "createdAt": "2026-03-21T10:00:00Z",
  "createdBy": "user-uuid",
  "isActive": true
}
```

---

## Field Types

### Supported Field Types

| Type | SurveyJS Type | Description | Example |
|------|---------------|-------------|---------|
| text | text | Single-line text input | Employee name |
| textarea | comment | Multi-line text input | Reason for leave |
| number | text | Numeric input (decimal) | Leave days |
| date | text | Date picker | Start date |
| datetime | text | Date-time picker | Deadline |
| email | text | Email input with validation | Contact email |
| select | dropdown | Single selection dropdown | Department |
| multiselect | dropdown | Multiple selection dropdown | Skills |
| radio | radiogroup | Radio button group | Approval decision |
| checkbox | boolean | Boolean checkbox | Requires receipt |
| file | file | File upload | Attach document |
| rating | rating | Star/number rating | Performance score |

---

## Field Definition Structure

```json
{
  "id": "startDate",
  "type": "date",
  "name": "startDate",
  "label": "Start Date",
  "placeholder": "Select your leave start date",
  "required": true,
  "readOnly": false,
  "visible": true,
  "defaultValue": null,
  "validation": {
    "rules": [
      {
        "type": "dateRange",
        "min": "today",
        "max": "+365d"
      },
      {
        "type": "businessDays",
        "excludeWeekends": true
      }
    ]
  },
  "condition": null,
  "permissions": {
    "read": ["USER", "MANAGER"],
    "write": ["USER"],
    "visible": ["USER", "MANAGER", "ADMIN"]
  },
  "dataSource": {
    "type": "dataDictionary",
    "key": "leaveTypes",
    "displayKey": "label",
    "valueKey": "value"
  },
  "helpText": "Select the first day of your leave",
  "infoText": "Cannot be in the past"
}
```

---

## Field Properties

### Required Properties

| Property | Type | Description |
|----------|------|-------------|
| id | string | Unique field identifier (camelCase) |
| type | string | Field type (see supported types) |
| name | string | Display label |
| label | string | Human-readable field label |
| required | boolean | Whether field is mandatory |
| visible | boolean | Whether field is visible in form |

### Optional Properties

| Property | Type | Description |
|----------|------|-------------|
| placeholder | string | Placeholder text |
| defaultValue | any | Default value |
| readOnly | boolean | Whether field is read-only |
| validation | object | Validation rules (see below) |
| condition | object | Conditional display logic |
| permissions | object | Role-based permissions |
| dataSource | object | Dynamic data source |
| helpText | string | Help tooltip |
| infoText | string | Additional information |

---

## Validation Rules

### Validation Types

#### Date Range
```json
{
  "type": "dateRange",
  "min": "today",
  "max": "+30d"
}
```

#### Text Length
```json
{
  "type": "minLength",
  "value": 10
},
{
  "type": "maxLength",
  "value": 500
}
```

#### Numeric Range
```json
{
  "type": "minValue",
  "value": 1
},
{
  "type": "maxValue",
  "value": 10
}
```

#### Pattern (Regex)
```json
{
  "type": "pattern",
  "value": "^[A-Z]{2}\\d{4}$",
  "message": "Invalid format. Use XX1234 format."
}
```

#### Custom Expression
```json
{
  "type": "expression",
  "expression": "{startDate} < {endDate}",
  "message": "End date must be after start date"
}
```

---

## Process Variable Mapping

### Mapping Structure

```json
{
  "processVariableMapping": {
    "startDate": "leaveStartDate",
    "endDate": "leaveEndDate",
    "reason": "leaveReason",
    "days": "leaveDaysCount",
    "type": "leaveType"
  }
}
```

### Mapping Rules

1. **Field ID → Variable Name**: Map form field ID to Flowable process variable name (camelCase)
2. **Type Conversion**:
   - text → String
   - number → Integer or Double (based on decimal presence)
   - date → ISO-8601 String (yyyy-MM-dd)
   - select/multiselect → Array of Strings
   - checkbox → Boolean
   - file → File metadata object (stored separately, variable contains file ID)

3. **Variable Size Limit**: Maximum 10KB per variable (constitution requirement)

4. **Nested Objects**: Form field can reference nested object properties:
   ```json
   {
     "id": "employee.name",
     "processVariableMapping": {
       "employee.name": "employeeName"
     }
   }
   ```

---

## Field-Level Permissions

### Permission Structure

```json
{
  "permissions": {
    "read": ["USER", "MANAGER", "ADMIN"],
    "write": ["MANAGER"],
    "visible": ["USER", "MANAGER", "ADMIN", "HR"]
  }
}
```

### Permission Levels

- **read**: Field value is visible and can be read
- **write**: Field value can be modified
- **visible**: Field appears in form (respects read/write settings)

### Role Resolution

- Roles are resolved from current user's roles in tenant context
- If user has ANY role in permission array, permission is granted
- Permission evaluation happens server-side before form rendering
- Client-side validation for immediate feedback, server-side validation for security

---

## Conditional Logic

### Field Display Conditions

```json
{
  "condition": {
    "type": "show",
    "expression": "{leaveType} === 'SICK_LEAVE'",
    "fields": ["sickLeaveCertificate", "doctorNote"]
  }
}
```

### Condition Types

- **show**: Show fields when expression evaluates to true
- **hide**: Hide fields when expression evaluates to true
- **require**: Make fields required when expression evaluates to true
- **readonly**: Make fields read-only when expression evaluates to true

### Expression Syntax

- Simple comparison: `{field} === 'value'`
- Numeric: `{days} > 5`
- Boolean: `{requiresApproval} === true`
- Multi-field: `{type} === 'URGENT' && {level} > 3`

---

## Data Dictionary Integration

### Data Source Configuration

```json
{
  "dataSource": {
    "type": "dataDictionary",
    "key": "leaveTypes",
    "displayKey": "label",
    "valueKey": "value"
  }
}
```

### Data Dictionary Structure

**Backend Entity**:
```java
@Entity
public class DataDictionary {
    @Id
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "key")
    private String key;

    @Column(name = "value", columnDefinition = "JSONB")
    private Map<String, Object> value;
}
```

**Example Data**:
```json
{
  "key": "leaveTypes",
  "value": [
    {"label": "Annual Leave", "value": "ANNUAL"},
    {"label": "Sick Leave", "value": "SICK"},
    {"label": "Personal Leave", "value": "PERSONAL"}
  ]
}
```

---

## Form Version Control

### Version Strategy

- Semantic versioning: `major.minor.patch`
- New version created when form schema changes
- Historical process instances preserve form version at completion time
- Latest version is determined by `version` field (DESC order)

### Version Retrieval

```sql
-- Get latest version for process definition
SELECT * FROM form_schemas
WHERE tenant_id = ?
  AND process_definition_key = ?
  AND is_active = true
ORDER BY version DESC
LIMIT 1;

-- Get form version for specific process instance
SELECT fs.* FROM form_schemas fs
INNER JOIN process_instances pi ON pi.form_schema_id = fs.id
WHERE pi.id = ?;
```

---

## Validation Workflow

### Client-Side Validation (SurveyJS)

1. User interacts with form
2. SurveyJS validates against validation rules
3. Immediate feedback for rule violations
4. Form submission blocked if validation fails

### Server-Side Validation (Before Process Variable Update)

1. Form submission received at backend
2. Validate form schema still exists and is active
3. Validate all required fields are present
4. Apply server-side validation rules (redundant check)
5. Map form data to process variables
6. Validate process variable size < 10KB per variable
7. Update Flowable process variables

### Validation Error Response

```json
{
  "success": false,
  "error": {
    "code": "FORM_VALIDATION_ERROR",
    "message": "Form validation failed",
    "details": {
      "fieldErrors": [
        {
          "field": "startDate",
          "message": "Start date cannot be in the past"
        },
        {
          "field": "reason",
          "message": "Reason must be at least 10 characters"
        }
      ]
    }
  }
}
```

---

## Form Rendering Integration

### Next.js Server Component

```typescript
// app/tasks/[id]/page.tsx
export default async function TaskPage({ params }) {
  const task = await fetchTask(params.id);
  const formSchema = await fetchFormSchema(task.formSchemaId);

  return (
    <div>
      <h1>{task.name}</h1>
      <SurveyFormRenderer
        formSchema={formSchema}
        taskId={task.id}
        onSubmit={handleFormSubmit}
      />
    </div>
  );
}
```

### Client Component Wrapper

```typescript
// components/forms/SurveyFormRenderer.tsx
'use client';

import { Survey } from 'surveyjs-react-ui';

export function SurveyFormRenderer({ formSchema, taskId, onSubmit }) {
  const survey = new Survey.Model(formSchema);

  survey.onComplete.add(async (sender) => {
    const formData = sender.data;
    await onSubmit(formData);
  });

  return <Survey model={survey} />;
}
```

---

## Form Submission Flow

```
1. User fills form and clicks Submit
   ↓
2. Client-side validation (SurveyJS)
   ↓
3. Form data sent to POST /tasks/{id}/complete
   ↓
4. Server-side validation
   ↓
5. Form data mapped to process variables
   ↓
6. Flowable task.complete() called
   ↓
7. Process advances to next node
   ↓
8. Task completion event logged (AuditLog)
   ↓
9. Response: { success: true, nextTask: {...} }
```

---

## Form Schema Lifecycle

### Creation

1. Business analyst uses visual form builder
2. Designer arranges fields, configures validation
3. System generates JSON schema
4. Form saved as FormSchema entity with version 1.0.0

### Update

1. Analyst modifies existing form
2. System validates changes
3. New FormSchema entity created with incremented version
4. Old FormSchema.isActive set to false
5. New process instances use new version
6. Historical instances continue using old version

### Deletion

1. FormSchema.isActive set to false (soft delete)
2. Form cannot be used for new process definitions
3. Historical instances with this form remain accessible

---

**Form schema contract complete and ready for visual builder implementation**.
