import { apiClient, ApiResponse } from './api';

export interface LoginRequest {
  username: string;
  password: string;
  tenantCode: string;
}

export interface LoginResponse {
  token: string;
  userId: string;
  username: string;
  tenantCode: string;
  tenantId: string;
}

export class AuthService {
  async login(request: LoginRequest): Promise<LoginResponse> {
    const response: ApiResponse<LoginResponse> = await apiClient.post('/api/auth/login', request);

    if (response.code === 'SUCCESS' && response.data) {
      // Store token and tenant ID
      apiClient.setToken(response.data.token);
      apiClient.setTenantId(response.data.tenantId);
      return response.data;
    }

    throw new Error(response.message || 'Login failed');
  }

  async logout() {
    await apiClient.post('/api/auth/logout', {});
    apiClient.clearAuth();
  }

  async getCurrentUser() {
    const response = await apiClient.get('/api/auth/me');
    return response.data;
  }

  isAuthenticated(): boolean {
    return !!apiClient.getToken();
  }
}

export const authService = new AuthService();
