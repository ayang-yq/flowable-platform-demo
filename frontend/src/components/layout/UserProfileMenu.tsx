'use client';

import { useState, useRef, useEffect } from 'react';
import { LogOut, ChevronUp } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';

interface UserProfileMenuProps {
  collapsed?: boolean;
}

export function UserProfileMenu({ collapsed }: UserProfileMenuProps) {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const handleClick = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false);
    };
    document.addEventListener('mousedown', handleClick);
    document.addEventListener('keydown', handleKey);
    return () => {
      document.removeEventListener('mousedown', handleClick);
      document.removeEventListener('keydown', handleKey);
    };
  }, [open]);

  if (!user) return null;

  const displayName = user.displayName || user.username;
  const initials = displayName
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  return (
    <div ref={menuRef} className="relative">
      <button
        onClick={() => setOpen(!open)}
        className={`flex items-center gap-3 w-full p-3 hover:bg-gray-50 transition-colors ${
          collapsed ? 'justify-center' : ''
        }`}
        aria-expanded={open}
        aria-haspopup="true"
      >
        <div className="flex-shrink-0 h-8 w-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-semibold">
          {initials}
        </div>
        {!collapsed && (
          <>
            <span className="flex-1 text-left text-sm font-medium text-gray-900 truncate">
              {displayName}
            </span>
            <ChevronUp
              className={`h-4 w-4 text-gray-400 transition-transform ${
                open ? '' : 'rotate-180'
              }`}
            />
          </>
        )}
      </button>

      {open && (
        <div className="absolute bottom-full left-0 right-0 mb-1 bg-white rounded-md shadow-lg border border-gray-200 py-1 z-50">
          <div className="px-3 py-2 border-b border-gray-100">
            <p className="text-sm font-medium text-gray-900">{displayName}</p>
            <p className="text-xs text-gray-500">{user.username}</p>
          </div>
          <button
            onClick={() => {
              setOpen(false);
              logout();
            }}
            className="flex items-center gap-2 w-full px-3 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
          >
            <LogOut className="h-4 w-4" />
            Log out
          </button>
        </div>
      )}
    </div>
  );
}
