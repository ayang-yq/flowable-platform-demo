'use client';

import { Activity, CheckCircle, Clock, User } from 'lucide-react';
import Link from 'next/link';

interface DashboardSummary {
  activeCount: number;
  completedCount: number;
  startedTodayCount: number;
  myActiveCount: number;
}

interface SummaryCardsProps {
  summary: DashboardSummary | null;
  loading?: boolean;
}

const cards = [
  { key: 'activeCount' as const, label: 'Active Instances', icon: Activity, color: 'text-green-600' },
  { key: 'completedCount' as const, label: 'Completed', icon: CheckCircle, color: 'text-blue-600' },
  { key: 'startedTodayCount' as const, label: 'Started Today', icon: Clock, color: 'text-orange-600' },
  { key: 'myActiveCount' as const, label: 'My Active', icon: User, color: 'text-purple-600' },
];

export default function SummaryCards({ summary, loading = false }: SummaryCardsProps) {
  if (loading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {cards.map((card) => (
          <div key={card.key} className="bg-white rounded-lg border border-gray-200 p-4 animate-pulse">
            <div className="h-4 bg-gray-100 rounded w-24 mb-2" />
            <div className="h-8 bg-gray-100 rounded w-16" />
          </div>
        ))}
      </div>
    );
  }

  if (!summary || (summary.activeCount === 0 && summary.completedCount === 0)) {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6 text-center">
        <p className="text-gray-500 mb-2">No instances yet.</p>
        <Link
          href="/workspace/start"
          className="text-blue-600 hover:text-blue-800 font-medium"
        >
          Get Started
        </Link>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      {cards.map((card) => {
        const Icon = card.icon;
        return (
          <div key={card.key} className="bg-white rounded-lg border border-gray-200 p-4">
            <div className="flex items-center gap-2 mb-1">
              <Icon className={`w-4 h-4 ${card.color}`} />
              <span className="text-sm text-gray-500">{card.label}</span>
            </div>
            <p className="text-2xl font-bold text-gray-900">{summary[card.key]}</p>
          </div>
        );
      })}
    </div>
  );
}
