'use client';

import { CheckCircle, PlayCircle, UserPlus } from 'lucide-react';

interface ActivityItem {
  id: string;
  type: string;
  title: string;
  timestamp: string;
  processDefinitionKey: string | null;
}

interface ActivityFeedProps {
  items: ActivityItem[] | null;
  isLoading?: boolean;
}

const typeConfig: Record<string, { icon: typeof CheckCircle; label: string; color: string }> = {
  TASK_COMPLETED: { icon: CheckCircle, label: 'Completed', color: 'text-green-600 bg-green-50' },
  PROCESS_STARTED: { icon: PlayCircle, label: 'Started', color: 'text-blue-600 bg-blue-50' },
  TASK_ASSIGNED: { icon: UserPlus, label: 'Assigned', color: 'text-purple-600 bg-purple-50' },
};

function formatTimestamp(ts: string): string {
  const date = new Date(ts);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins}m ago`;
  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `${diffHours}h ago`;
  const diffDays = Math.floor(diffHours / 24);
  return `${diffDays}d ago`;
}

function SkeletonItem() {
  return (
    <div className="flex items-center gap-3 py-3">
      <div className="h-8 w-8 bg-gray-200 rounded-full animate-pulse" />
      <div className="flex-1">
        <div className="h-4 w-48 bg-gray-200 rounded animate-pulse" />
        <div className="h-3 w-24 bg-gray-200 rounded animate-pulse mt-1" />
      </div>
    </div>
  );
}

export function ActivityFeed({ items, isLoading }: ActivityFeedProps) {
  return (
    <div className="bg-white rounded-lg border border-gray-200 p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-900">Recent Activity</h3>
      </div>

      {isLoading ? (
        <div className="divide-y divide-gray-100">
          {Array.from({ length: 5 }).map((_, i) => (
            <SkeletonItem key={i} />
          ))}
        </div>
      ) : !items || items.length === 0 ? (
        <p className="text-sm text-gray-400 py-4">
          No recent activity. Start a process or complete a task to see activity here.
        </p>
      ) : (
        <div className="divide-y divide-gray-100">
          {items.map((item) => {
            const config = typeConfig[item.type] || typeConfig.TASK_COMPLETED;
            const Icon = config.icon;
            return (
              <div key={item.id} className="flex items-center gap-3 py-3">
                <div className={`flex-shrink-0 p-1.5 rounded-full ${config.color}`}>
                  <Icon className="h-4 w-4" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">{item.title}</p>
                  <p className="text-xs text-gray-500">
                    {config.label} {formatTimestamp(item.timestamp)}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
