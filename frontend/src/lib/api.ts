const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export interface ApiResponse<T = any> {
  timestamp: string;
  code: string;
  message: string;
  data: T;
}

export interface DiagramData {
  diagramXml: string;
  activeElementIds: string[];
  completedElementIds: string[];
  currentElementId: string | null;
}

class ApiClient {
  private baseUrl: string;
  private token: string | null = null;
  private tenantId: string | null = null;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  setToken(token: string) {
    this.token = token;
    if (typeof window !== 'undefined') {
      localStorage.setItem('auth_token', token);
    }
  }

  getToken(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('auth_token') || this.token;
    }
    return this.token;
  }

  setTenantId(tenantId: string) {
    this.tenantId = tenantId;
    if (typeof window !== 'undefined') {
      localStorage.setItem('tenant_id', tenantId);
    }
  }

  getTenantId(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('tenant_id') || this.tenantId;
    }
    return this.tenantId;
  }

  clearAuth() {
    this.token = null;
    this.tenantId = null;
    if (typeof window !== 'undefined') {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('tenant_id');
    }
  }

  private getHeaders(): HeadersInit {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const tenantId = this.getTenantId();
    if (tenantId) {
      headers['X-Tenant-Id'] = tenantId;
    }

    return headers;
  }

  async get<T>(path: string): Promise<ApiResponse<T>> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: 'GET',
      headers: this.getHeaders(),
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.statusText}`);
    }

    return response.json();
  }

  async post<T>(path: string, data: any): Promise<ApiResponse<T>> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.statusText}`);
    }

    return response.json();
  }

  async put<T>(path: string, data: any): Promise<ApiResponse<T>> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.statusText}`);
    }

    return response.json();
  }

  async delete<T>(path: string): Promise<ApiResponse<T>> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: 'DELETE',
      headers: this.getHeaders(),
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.statusText}`);
    }

    return response.json();
  }

  /**
   * Fetch BPMN diagram data for a process instance
   */
  async getProcessInstanceDiagram(processInstanceId: string): Promise<DiagramData> {
    const response = await this.get<DiagramData>(
      `/api/workspace/process-instances/${processInstanceId}/diagram`
    );
    return response.data;
  }

  /**
   * Fetch CMMN diagram data for a case instance
   */
  async getCaseInstanceDiagram(caseInstanceId: string): Promise<DiagramData> {
    const response = await this.get<DiagramData>(
      `/api/workspace/case-instances/${caseInstanceId}/diagram`
    );
    return response.data;
  }

  /**
   * Fetch task details by ID
   */
  async getTask(taskId: string): Promise<any> {
    const response = await this.get<any>(`/api/tasks/${taskId}`);
    return response.data;
  }

  /**
   * Claim an unassigned task
   */
  async claimTask(taskId: string): Promise<void> {
    await this.post(`/api/tasks/${taskId}/claim`, {});
  }

  /**
   * Complete a task with optional variables
   */
  async completeTask(taskId: string, variables?: Record<string, any>): Promise<void> {
    await this.post(`/api/tasks/${taskId}/complete`, { variables: variables || {} });
  }

  /**
   * Delegate a task to another user
   */
  async delegateTask(taskId: string, delegateTo: string): Promise<void> {
    await this.post(`/api/tasks/${taskId}/delegate`, { delegateTo });
  }
}

export const apiClient = new ApiClient(API_URL);
