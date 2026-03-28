'use client';

import { TaskDTO } from '@/types/task';
import { formatDistanceToNow } from 'date-fns';

interface TaskCardProps {
  task: TaskDTO;
  onClaim?: (taskId: string) => void;
  onComplete?: (taskId: string) => void;
  onDelegate?: (taskId: string) => void;
  onViewDetails?: (taskId: string) => void;
  showActions?: boolean;
}

export default function TaskCard({
  task,
  onClaim,
  onComplete,
  onDelegate,
  onViewDetails,
  showActions = true,
}: TaskCardProps) {
  const isOverdue = task.dueDate && new Date(task.dueDate) < new Date();
  const isHighPriority = task.priority >= 70;
  const isMediumPriority = task.priority >= 40 && task.priority < 70;
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const isLowPriority = task.priority < 40;

  const getPriorityBadge = () => {
    if (isHighPriority) {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
          High Priority
        </span>
      );
    }
    if (isMediumPriority) {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
          Medium Priority
        </span>
      );
    }
    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
        Low Priority
      </span>
    );
  };

  const getOverdueBadge = () => {
    if (!isOverdue) return null;

    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-600 text-white animate-pulse">
        Overdue
      </span>
    );
  };

  const getDueDateText = () => {
    if (!task.dueDate) return null;

    const dueDate = new Date(task.dueDate);
    const now = new Date();
    const distance = formatDistanceToNow(dueDate, { addSuffix: true });

    if (isOverdue) {
      return <span className="text-red-600 font-medium">Overdue by {distance}</span>;
    }

    const daysUntilDue = Math.ceil((dueDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    if (daysUntilDue <= 1) {
      return <span className="text-orange-600 font-medium">Due {distance}</span>;
    }
    return <span className="text-gray-600">Due {distance}</span>;
  };

  return (
    <div className={`bg-white rounded-lg shadow-sm border-2 transition-all hover:shadow-md ${
      isOverdue ? 'border-red-300 bg-red-50' : 'border-gray-200'
    }`}>
      <div className="p-6">
        <div className="flex items-start justify-between mb-4">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <h3 className="text-lg font-semibold text-gray-900">{task.name}</h3>
              {getPriorityBadge()}
              {getOverdueBadge()}
            </div>
            {task.description && (
              <p className="text-sm text-gray-600 mb-2">{task.description}</p>
            )}
            <div className="flex items-center gap-4 text-sm text-gray-600">
              {task.assignee && (
                <div className="flex items-center gap-1">
                  <svg
                    className="w-4 h-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                    />
                  </svg>
                  <span>Assigned to: {task.assignee}</span>
                </div>
              )}
              {task.dueDate && (
                <div className="flex items-center gap-1">
                  <svg
                    className="w-4 h-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                  {getDueDateText()}
                </div>
              )}
            </div>
          </div>
        </div>

        {task.ccUsers && task.ccUsers.length > 0 && (
          <div className="mb-4">
            <p className="text-xs text-gray-500 mb-1">CC Users:</p>
            <div className="flex flex-wrap gap-1">
              {task.ccUsers.map((user) => (
                <span
                  key={user}
                  className="inline-flex items-center px-2 py-1 rounded text-xs bg-blue-50 text-blue-700"
                >
                  {user}
                </span>
              ))}
            </div>
          </div>
        )}

        {showActions && (
          <div className="flex items-center justify-between pt-4 border-t border-gray-200">
            <div className="flex gap-2">
              {onViewDetails && (
                <button
                  onClick={() => onViewDetails(task.id)}
                  className="px-3 py-1.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  View Details
                </button>
              )}
            </div>
            <div className="flex gap-2">
              {!task.assignee && onClaim && (
                <button
                  onClick={() => onClaim(task.id)}
                  className="px-3 py-1.5 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  Claim Task
                </button>
              )}
              {onComplete && (
                <button
                  onClick={() => onComplete(task.id)}
                  className="px-3 py-1.5 text-sm font-medium text-white bg-green-600 rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
                >
                  Complete
                </button>
              )}
              {onDelegate && task.assignee && (
                <button
                  onClick={() => onDelegate(task.id)}
                  className="px-3 py-1.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
                >
                  Delegate
                </button>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
