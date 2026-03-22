'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { PanelLeftClose, PanelLeft } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { navigationItems } from './navigation-items';
import { UserProfileMenu } from './UserProfileMenu';

const COLLAPSED_KEY = 'sidebar_collapsed';

export function Sidebar() {
  const pathname = usePathname();
  const { user } = useAuth();
  const [collapsed, setCollapsed] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem(COLLAPSED_KEY);
    if (stored === 'true') setCollapsed(true);
  }, []);

  const toggleCollapsed = () => {
    const next = !collapsed;
    setCollapsed(next);
    localStorage.setItem(COLLAPSED_KEY, String(next));
  };

  const userRoles = user?.roles || [];

  const visibleItems = navigationItems.filter(
    (item) => !item.requiredRole || userRoles.includes(item.requiredRole)
  );

  const isActive = (path: string) => {
    if (path === '/home') return pathname === '/home';
    return pathname.startsWith(path);
  };

  return (
    <nav
      role="navigation"
      aria-label="Main navigation"
      className={`flex flex-col h-screen bg-white border-r border-gray-200 transition-all duration-200 ${
        collapsed ? 'w-16' : 'w-60'
      }`}
    >
      <div className="flex items-center justify-between p-4 border-b border-gray-100">
        {!collapsed && (
          <span className="text-lg font-bold text-gray-900">Flowable</span>
        )}
        <button
          onClick={toggleCollapsed}
          className="p-1.5 rounded-md hover:bg-gray-100 text-gray-500"
          aria-expanded={!collapsed}
          aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {collapsed ? (
            <PanelLeft className="h-5 w-5" />
          ) : (
            <PanelLeftClose className="h-5 w-5" />
          )}
        </button>
      </div>

      <div className="flex-1 py-4 overflow-y-auto">
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
                  } ${collapsed ? 'justify-center' : ''}`}
                  title={collapsed ? item.label : undefined}
                >
                  <Icon className="h-5 w-5 flex-shrink-0" />
                  {!collapsed && <span>{item.label}</span>}
                </Link>
              </li>
            );
          })}
        </ul>
      </div>

      <div className="border-t border-gray-100">
        <UserProfileMenu collapsed={collapsed} />
      </div>
    </nav>
  );
}
