'use client';

import { useState, useEffect, useRef } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Menu, X } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { navigationItems } from './navigation-items';

export function MobileNav() {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const drawerRef = useRef<HTMLDivElement>(null);

  const userRoles = user?.roles || [];

  const visibleItems = navigationItems.filter(
    (item) => !item.requiredRole || userRoles.includes(item.requiredRole)
  );

  const isActive = (path: string) => {
    if (path === '/home') return pathname === '/home';
    return pathname.startsWith(path);
  };

  // Close drawer on navigation
  useEffect(() => {
    setOpen(false);
  }, [pathname]);

  // Close on click outside
  useEffect(() => {
    if (!open) return;
    const handleClick = (e: MouseEvent) => {
      if (drawerRef.current && !drawerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, [open]);

  // Close on Escape
  useEffect(() => {
    if (!open) return;
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false);
    };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [open]);

  return (
    <>
      <header className="sticky top-0 z-40 flex items-center justify-between bg-white border-b border-gray-200 px-4 py-3">
        <button
          onClick={() => setOpen(true)}
          className="p-2 rounded-md hover:bg-gray-100 text-gray-600"
          aria-label="Open navigation menu"
        >
          <Menu className="h-5 w-5" />
        </button>
        <span className="text-lg font-bold text-gray-900">Flowable</span>
        <div className="w-9" /> {/* Spacer for centering */}
      </header>

      {/* Overlay */}
      {open && (
        <div className="fixed inset-0 z-50 bg-black/30" aria-hidden="true" />
      )}

      {/* Drawer */}
      <div
        ref={drawerRef}
        className={`fixed top-0 left-0 z-50 h-full w-64 bg-white shadow-lg transform transition-transform duration-200 ${
          open ? 'translate-x-0' : '-translate-x-full'
        }`}
        role="dialog"
        aria-modal={open}
        aria-label="Navigation menu"
      >
        <div className="flex items-center justify-between p-4 border-b border-gray-100">
          <span className="text-lg font-bold text-gray-900">Flowable</span>
          <button
            onClick={() => setOpen(false)}
            className="p-1.5 rounded-md hover:bg-gray-100 text-gray-500"
            aria-label="Close navigation menu"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <nav className="py-4" role="navigation" aria-label="Main navigation">
          <ul className="space-y-1 px-2">
            {visibleItems.map((item) => {
              const Icon = item.icon;
              const active = isActive(item.path);
              return (
                <li key={item.path}>
                  <Link
                    href={item.path}
                    aria-current={active ? 'page' : undefined}
                    className={`flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                      active
                        ? 'bg-blue-50 text-blue-700'
                        : 'text-gray-700 hover:bg-gray-100'
                    }`}
                  >
                    <Icon className="h-5 w-5 flex-shrink-0" />
                    <span>{item.label}</span>
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        {user && (
          <div className="absolute bottom-0 left-0 right-0 border-t border-gray-100 p-4">
            <p className="text-sm font-medium text-gray-900 truncate">
              {user.displayName}
            </p>
            <button
              onClick={logout}
              className="mt-2 text-sm text-red-600 hover:text-red-700"
            >
              Log out
            </button>
          </div>
        )}
      </div>
    </>
  );
}
