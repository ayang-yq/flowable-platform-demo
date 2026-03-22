'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/contexts/AuthContext';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth();
  const router = useRouter();

  const isAdmin = user?.roles?.includes('ADMIN') ?? false;

  useEffect(() => {
    if (!isLoading && !isAdmin) {
      router.replace('/home');
    }
  }, [isLoading, isAdmin, router]);

  if (isLoading || !isAdmin) {
    return null;
  }

  return <>{children}</>;
}
