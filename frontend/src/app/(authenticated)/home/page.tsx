'use client';

import { useEffect, useState } from 'react';
import { CheckSquare, GitBranch, FileText, BarChart3 } from 'lucide-react';
import { apiClient } from '@/lib/api';
import { useAuth } from '@/contexts/AuthContext';
import { SummaryCard } from '@/components/home/SummaryCard';
import { ActivityFeed } from '@/components/home/ActivityFeed';
import { QuickActionCard } from '@/components/home/QuickActionCard';

interface ActivityItem {
  id: string;
  type: string;
  title: string;
  timestamp: string;
  processDefinitionKey: string | null;
}

interface HomeSummary {
  pendingTaskCount: number;
  activeProcessCount: number;
  recentActivity: ActivityItem[];
}

export default function HomePage() {
  const { user } = useAuth();
  const [summary, setSummary] = useState<HomeSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchSummary() {
      try {
        const response = await apiClient.get<HomeSummary>('/api/home/summary');
        if (response.code === 'SUCCESS' && response.data) {
          setSummary(response.data);
        } else {
          setError('Unable to load summary data');
        }
      } catch {
        setError('Unable to load summary data');
      } finally {
        setIsLoading(false);
      }
    }
    fetchSummary();
  }, []);

  const displayName = user?.displayName || user?.username || 'User';

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">
          Welcome back, {displayName}
        </h1>
        <p className="text-gray-500 mt-1">Here&apos;s an overview of your work</p>
      </div>

      {error && !isLoading && (
        <div className="mb-6 bg-yellow-50 border border-yellow-200 text-yellow-800 px-4 py-3 rounded">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-8">
        <SummaryCard
          icon={CheckSquare}
          label="Pending Tasks"
          count={summary?.pendingTaskCount ?? null}
          href="/tasks"
          isLoading={isLoading}
          isEmpty={summary?.pendingTaskCount === 0}
          emptyMessage="No pending tasks"
        />
        <SummaryCard
          icon={GitBranch}
          label="Active Processes"
          count={summary?.activeProcessCount ?? null}
          href="/processes"
          isLoading={isLoading}
          isEmpty={summary?.activeProcessCount === 0}
          emptyMessage="No active processes"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <div className="lg:col-span-2">
          <ActivityFeed
            items={summary?.recentActivity ?? null}
            isLoading={isLoading}
          />
        </div>
        <div>
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h3>
          <div className="space-y-3">
            <QuickActionCard
              icon={CheckSquare}
              label="Tasks"
              description="View and manage your tasks"
              href="/tasks"
            />
            <QuickActionCard
              icon={GitBranch}
              label="Processes"
              description="Start or track processes"
              href="/processes"
            />
            <QuickActionCard
              icon={FileText}
              label="Forms"
              description="Build and manage forms"
              href="/forms/builder"
            />
            <QuickActionCard
              icon={BarChart3}
              label="Dashboard"
              description="View analytics and reports"
              href="/dashboard"
            />
          </div>
        </div>
      </div>
    </div>
  );
}
