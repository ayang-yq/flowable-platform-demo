'use client';

import { useState, useEffect, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import dynamic from 'next/dynamic';
import { ArrowLeft, User, Clock, Tag, Link2, AlertCircle, FileText } from 'lucide-react';
import { apiClient } from '@/lib/api';
import { TaskDTO } from '@/types/task';

const SurveyFormRenderer = dynamic(
  () => import('@/components/forms/SurveyFormRenderer'),
  { ssr: false, loading: () => <div className="animate-pulse h-48 bg-gray-100 rounded" /> }
);

function formatDate(dateStr: string | null): string {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString();
}

export default function TaskDetailPage() {
  const params = useParams();
  const router = useRouter();
  const taskId = params.id as string;

  const [task, setTask] = useState<TaskDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [formSchema, setFormSchema] = useState<any>(null);

  useEffect(() => {
    const fetchTask = async () => {
      try {
        const data = await apiClient.getTask(taskId);
        setTask(data);
        setError(null);

        // Fetch form schema if task has a definition key
        if (data.processDefinitionKey) {
          try {
            const formResponse = await apiClient.get<any>(`/api/forms/task-definition/${data.processDefinitionKey}/active`);
            if (formResponse) {
              setFormSchema(formResponse);
            }
          } catch {
            // No form schema available, that's fine
          }
        }
      } catch {
        setError('Task not found');
      } finally {
        setLoading(false);
      }
    };
    fetchTask();
  }, [taskId]);

  const handleClaim = async () => {
    if (!task) return;
    setActionLoading(true);
    setSuccessMsg(null);
    try {
      await apiClient.claimTask(task.id);
      const updated = await apiClient.getTask(task.id);
      setTask(updated);
      setSuccessMsg('Task claimed successfully');
    } catch {
      setSuccessMsg(null);
      setError('Failed to claim task. It may have been claimed by someone else.');
    } finally {
      setActionLoading(false);
    }
  };

  const [formValues, setFormValues] = useState<Record<string, any>>({});

  const handleFormChange = useCallback((data: Record<string, any>) => {
    setFormValues(data);
  }, []);

  const handleComplete = async () => {
    if (!task) return;
    setActionLoading(true);
    setSuccessMsg(null);
    try {
      await apiClient.completeTask(task.id, Object.keys(formValues).length > 0 ? formValues : undefined);
      setSuccessMsg('Task completed. Redirecting to instance...');
      setTimeout(() => {
        if (task.processInstanceId) {
          router.push(`/workspace/${task.processInstanceId}?type=BPMN`);
        } else {
          router.push('/tasks');
        }
      }, 1000);
    } catch {
      setSuccessMsg(null);
      setError('Failed to complete task. Please try again.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelegate = async () => {
    if (!task) return;
    const username = prompt('Enter username to delegate task to:');
    if (!username) return;
    setActionLoading(true);
    setSuccessMsg(null);
    try {
      await apiClient.delegateTask(task.id, username);
      const updated = await apiClient.getTask(task.id);
      setTask(updated);
      setSuccessMsg(`Task delegated to ${username}`);
    } catch {
      setSuccessMsg(null);
      setError('Failed to delegate task.');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="animate-pulse space-y-4">
            <div className="h-8 bg-gray-200 rounded w-1/3" />
            <div className="h-48 bg-gray-200 rounded" />
          </div>
        </div>
      </div>
    );
  }

  if (error || !task) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="bg-red-50 border border-red-200 rounded-md p-4 text-red-700">
            {error || 'Task not found'}
          </div>
          <button
            onClick={() => router.push('/tasks')}
            className="mt-4 text-blue-600 hover:text-blue-800"
          >
            Back to Tasks
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center gap-4">
            <button
              onClick={() => router.push('/tasks')}
              className="text-gray-500 hover:text-gray-700"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <h1 className="text-2xl font-bold text-gray-900 truncate">{task.name}</h1>
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* Success message */}
        {successMsg && (
          <div className="bg-green-50 border border-green-200 rounded-md p-4 text-green-700">
            {successMsg}
          </div>
        )}

        {/* Error message */}
        {error && !successMsg && (
          <div className="bg-red-50 border border-red-200 rounded-md p-4 text-red-700 flex items-center gap-2">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            {error}
          </div>
        )}

        {/* Task Metadata */}
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Task Details</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="flex items-start gap-2">
              <User className="w-4 h-4 text-gray-400 mt-0.5" />
              <div>
                <p className="text-xs text-gray-500">Assignee</p>
                <p className="text-sm font-medium text-gray-900">{task.assignee || 'Unassigned'}</p>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <Clock className="w-4 h-4 text-gray-400 mt-0.5" />
              <div>
                <p className="text-xs text-gray-500">Created</p>
                <p className="text-sm text-gray-900">{formatDate(task.createTime)}</p>
              </div>
            </div>
            {task.dueDate && (
              <div className="flex items-start gap-2">
                <Clock className="w-4 h-4 text-gray-400 mt-0.5" />
                <div>
                  <p className="text-xs text-gray-500">Due Date</p>
                  <p className={`text-sm font-medium ${new Date(task.dueDate) < new Date() ? 'text-red-600' : 'text-gray-900'}`}>
                    {formatDate(task.dueDate)}
                  </p>
                </div>
              </div>
            )}
            <div className="flex items-start gap-2">
              <Tag className="w-4 h-4 text-gray-400 mt-0.5" />
              <div>
                <p className="text-xs text-gray-500">Priority</p>
                <p className="text-sm font-medium text-gray-900">
                  {task.priority >= 70 ? 'High' : task.priority >= 40 ? 'Medium' : 'Low'}
                </p>
              </div>
            </div>
            {task.processInstanceId && (
              <div className="flex items-start gap-2">
                <Link2 className="w-4 h-4 text-gray-400 mt-0.5" />
                <div>
                  <p className="text-xs text-gray-500">Process Instance</p>
                  <button
                    onClick={() => router.push(`/workspace/${task.processInstanceId}?type=BPMN`)}
                    className="text-sm font-medium text-blue-600 hover:text-blue-800 hover:underline"
                  >
                    {task.processInstanceId}
                  </button>
                </div>
              </div>
            )}
            {task.category && (
              <div className="flex items-start gap-2">
                <Tag className="w-4 h-4 text-gray-400 mt-0.5" />
                <div>
                  <p className="text-xs text-gray-500">Category</p>
                  <p className="text-sm font-medium text-gray-900">{task.category}</p>
                </div>
              </div>
            )}
          </div>
          {task.description && (
            <div className="mt-4 pt-4 border-t border-gray-200">
              <p className="text-xs text-gray-500 mb-1">Description</p>
              <p className="text-sm text-gray-700">{task.description}</p>
            </div>
          )}
        </div>

        {/* Form */}
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Task Form</h2>
          {formSchema ? (
            <SurveyFormRenderer
              schema={formSchema}
              onDataChange={handleFormChange}
            />
          ) : (
            <div className="flex items-center gap-2 text-sm text-gray-500">
              <FileText className="w-4 h-4" />
              <p>No form required for this task</p>
            </div>
          )}
        </div>

        {/* Actions */}
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Actions</h2>
          <div className="flex gap-3">
            {!task.assignee && (
              <button
                onClick={async () => { await handleClaim(); }}
                disabled={actionLoading}
                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                Claim Task
              </button>
            )}
            <button
              onClick={async () => {
                if (!task.assignee) {
                  setActionLoading(true);
                  try {
                    await apiClient.claimTask(task.id);
                    const updated = await apiClient.getTask(task.id);
                    setTask(updated);
                  } catch {
                    setError('Failed to claim task.');
                    setActionLoading(false);
                    return;
                  }
                }
                handleComplete();
              }}
              disabled={actionLoading}
              className="px-4 py-2 text-sm font-medium text-white bg-green-600 rounded-md hover:bg-green-700 disabled:opacity-50"
            >
              {task.assignee ? 'Complete' : 'Claim & Complete'}
            </button>
            {task.assignee && (
              <button
                onClick={handleDelegate}
                disabled={actionLoading}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50"
              >
                Delegate
              </button>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
