'use client';

import Link from 'next/link';
import { LucideIcon } from 'lucide-react';

interface QuickActionCardProps {
  icon: LucideIcon;
  label: string;
  description: string;
  href: string;
}

export function QuickActionCard({ icon: Icon, label, description, href }: QuickActionCardProps) {
  return (
    <Link
      href={href}
      className="flex items-center gap-4 bg-white rounded-lg border border-gray-200 p-4 hover:shadow-md hover:border-blue-200 transition-all"
    >
      <div className="flex-shrink-0 p-2 bg-gray-50 rounded-lg">
        <Icon className="h-5 w-5 text-gray-600" />
      </div>
      <div>
        <p className="text-sm font-semibold text-gray-900">{label}</p>
        <p className="text-xs text-gray-500">{description}</p>
      </div>
    </Link>
  );
}
