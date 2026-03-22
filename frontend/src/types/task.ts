export interface TaskDTO {
  id: string;
  name: string;
  description?: string;
  assignee?: string;
  processInstanceId: string;
  processDefinitionKey?: string;
  createTime: string;
  dueDate?: string;
  priority: number;
  category?: string;
  ccUsers?: string[];
}

export interface TaskPageResponse {
  content: TaskDTO[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface TaskFilters {
  department?: string;
  priority?: 'high' | 'medium' | 'low';
  dueBefore?: string;
}

export interface CompleteTaskRequest {
  variables: Record<string, any>;
}

export interface DelegateTaskRequest {
  delegateTo: string;
}

export interface CcUsersRequest {
  ccUsers: string[];
}
