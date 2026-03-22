export interface FormField {
  name: string;
  type: 'text' | 'number' | 'date' | 'select' | 'radio' | 'checkbox' | 'file' | 'textarea';
  label: string;
  isRequired?: boolean;
  choices?: string[];
  defaultValue?: any;
  placeholder?: string;
  maxLength?: number;
  minLength?: number;
  min?: number;
  max?: number;
  pattern?: string;
}

export interface FormSchema {
  id?: string;
  name: string;
  description?: string;
  version: number;
  schema: string; // JSON string
  validationRules?: string; // JSON string
  fieldPermissions?: FieldPermissions;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  processDefinitionKey?: string;
  taskDefinitionKey?: string;
}

export interface FieldPermissions {
  [fieldName: string]: {
    permission: 'editable' | 'read-only' | 'hidden';
    roles: string[];
  };
}

export interface FormValidationResult {
  valid: boolean;
  errors: string[];
  warnings: string[];
  permissionWarnings: Record<string, string>;
}

export interface FormListResponse {
  content: FormSchema[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface CreateFormRequest {
  name: string;
  description?: string;
  schema: string;
  validationRules?: string;
  fieldPermissions?: string;
}

export interface UpdateFormRequest {
  schema: string;
}

export interface ValidateFormDataRequest {
  [fieldName: string]: any;
}
