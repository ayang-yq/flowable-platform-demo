'use client';

import Link from 'next/link';
import { LucideIcon } from 'lucide-react';

interface SummaryCardProps {
  icon: LucideIcon;
  label: string;
  count: number | null;
  href: string;
  isLoading?: boolean;
  isEmpty?: boolean;
  emptyMessage?: string;
}

export function SummaryCard({ icon: Icon, label, count, href, isLoading, isEmpty, emptyMessage }: SummaryCardProps) {
  return (
    <Link
      href={href}
      className="block bg-white rounded-lg border border-gray-200 p-6 hover:shadow-md transition-shadow"
    >
      <div className="flex items-center gap-4">
        <div className="flex-shrink-0 p-3 bg-blue-50 rounded-lg">
          <Icon className="h-6 w-6 text-blue-600" />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-gray-500">{label}</p>
          {isLoading ? (
            <div className="h-8 w-16 bg-gray-200 rounded animate-pulse mt-1" />
          ) : isEmpty ? (
            <p className="text-sm text-gray-400 mt-1">{emptyMessage || 'None'}</p>
          ) : (
            <p className="text-2xl font-bold text-gray-900">{count}</p>
          )}
        </div>
      </div>
    </Link>
  );
}
