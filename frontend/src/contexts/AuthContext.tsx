'use client';

import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { authService } from '@/lib/auth';
import { useRouter } from 'next/navigation';

export interface UserInfo {
  userId: string;
  username: string;
  displayName: string;
  tenantCode: string;
  tenantId: string;
  roles: string[];
}

interface AuthContextType {
  user: UserInfo | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  isAuthenticated: false,
  isLoading: true,
  logout: async () => {},
});

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const userInfo = authService.getUserInfo();
    if (userInfo && authService.isAuthenticated()) {
      setUser(userInfo);
    }
    setIsLoading(false);
  }, []);

  const logout = async () => {
    try {
      await authService.logout();
    } catch {
      // Clear auth even if API call fails
    }
    setUser(null);
    router.push('/login');
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, isLoading, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
