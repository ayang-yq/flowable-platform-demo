'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { TaskDTO, TaskPageResponse } from '@/types/task';
import { apiClient } from '@/lib/api';
import TaskCard from './TaskCard';

interface TaskListContainerProps {
  initialData: TaskPageResponse;
}

export default function TaskListContainer({ initialData }: TaskListContainerProps) {
  const router = useRouter();
  const [tasks, setTasks] = useState<TaskDTO[]>(initialData.content);
  const [, setActionLoading] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleClaim = async (taskId: string) => {
    setActionLoading(taskId);
    setError(null);
    // Optimistic update
    setTasks(prev => prev.map(t => t.id === taskId ? { ...t, assignee: 'current' } : t));
    try {
      await apiClient.claimTask(taskId);
      const updated = await apiClient.getTask(taskId);
      setTasks(prev => prev.map(t => t.id === taskId ? updated : t));
    } catch {
      // Revert optimistic update
      setTasks(prev => prev.map(t => t.id === taskId ? { ...t, assignee: null } : t));
      setError('Failed to claim task. It may have been claimed by someone else.');
    } finally {
      setActionLoading(null);
    }
  };

  const handleComplete = async (taskId: string) => {
    setActionLoading(taskId);
    setError(null);
    // Optimistic update - remove from list
    const removed = tasks.find(t => t.id === taskId);
    setTasks(prev => prev.filter(t => t.id !== taskId));
    try {
      // Claim first if unassigned
      if (removed && !removed.assignee) {
        await apiClient.claimTask(taskId);
      }
      await apiClient.completeTask(taskId);
      // Task completed, stay removed
    } catch {
      // Revert - add back
      if (removed) {
        setTasks(prev => [...prev, removed]);
      }
      setError('Failed to complete task. Please try again.');
    } finally {
      setActionLoading(null);
    }
  };

  const handleViewDetails = (taskId: string) => {
    router.push(`/tasks/${taskId}`);
  };

  return (
    <>
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4 text-red-700 mb-4">
          {error}
        </div>
      )}
      <div className="space-y-4">
        {tasks.length === 0 ? (
          <div className="bg-white shadow rounded-lg p-6 text-center">
            <svg
              className="mx-auto h-12 w-12 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
              />
            </svg>
            <h3 className="mt-2 text-sm font-medium text-gray-900">No tasks</h3>
            <p className="mt-1 text-sm text-gray-500">
              No tasks assigned to you at the moment.
            </p>
          </div>
        ) : (
          tasks.map((task) => (
            <TaskCard
              key={task.id}
              task={task}
              showActions={true}
              onClaim={handleClaim}
              onComplete={handleComplete}
              onViewDetails={handleViewDetails}
            />
          ))
        )}
      </div>
    </>
  );
}
