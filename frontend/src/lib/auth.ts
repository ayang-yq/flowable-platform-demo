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
  displayName: string | null;
  tenantCode: string;
  tenantId: string;
  roles: string[];
}

export class AuthService {
  async login(request: LoginRequest): Promise<LoginResponse> {
    const response: ApiResponse<LoginResponse> = await apiClient.post('/api/auth/login', request);

    if (response.code === 'SUCCESS' && response.data) {
      apiClient.setToken(response.data.token);
      apiClient.setTenantId(response.data.tenantId);
      this.storeUserInfo(response.data);
      return response.data;
    }

    throw new Error(response.message || 'Login failed');
  }

  async logout() {
    try {
      await apiClient.post('/api/auth/logout', {});
    } catch {
      // Ignore API errors during logout
    }
    apiClient.clearAuth();
    this.clearUserInfo();
  }

  async getCurrentUser() {
    const response = await apiClient.get('/api/auth/me');
    return response.data;
  }

  isAuthenticated(): boolean {
    return !!apiClient.getToken();
  }

  storeUserInfo(data: LoginResponse) {
    if (typeof window !== 'undefined') {
      localStorage.setItem('user_id', data.userId);
      localStorage.setItem('username', data.username);
      localStorage.setItem('display_name', data.displayName || data.username);
      localStorage.setItem('tenant_code', data.tenantCode);
      localStorage.setItem('roles', JSON.stringify(data.roles || []));
    }
  }

  getUserInfo(): { userId: string; username: string; displayName: string; tenantCode: string; tenantId: string; roles: string[] } | null {
    if (typeof window === 'undefined') return null;
    const token = apiClient.getToken();
    if (!token) return null;
    const userId = localStorage.getItem('user_id');
    const username = localStorage.getItem('username');
    if (!userId || !username) return null;
    return {
      userId,
      username,
      displayName: localStorage.getItem('display_name') || username,
      tenantCode: localStorage.getItem('tenant_code') || '',
      tenantId: apiClient.getTenantId() || '',
      roles: JSON.parse(localStorage.getItem('roles') || '[]'),
    };
  }

  clearUserInfo() {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('user_id');
      localStorage.removeItem('username');
      localStorage.removeItem('display_name');
      localStorage.removeItem('tenant_code');
      localStorage.removeItem('roles');
    }
  }
}

export const authService = new AuthService();
