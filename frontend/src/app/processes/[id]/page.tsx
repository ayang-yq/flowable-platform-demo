'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { apiClient, ApiResponse } from '@/lib/api';

interface ProcessDetail {
  processInstanceId: string;
  processDefinitionKey: string;
  businessKey: string;
  suspended: boolean;
  variables: Record<string, any>;
  startTime: string;
}

interface Task {
  id: string;
  name: string;
  assignee: string;
  createTime: string;
}

export default function ProcessDetailPage() {
  const params = useParams();
  const router = useRouter();
  const [process, setProcess] = useState<ProcessDetail | null>(null);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (params.id) {
      fetchProcessDetails();
      fetchProcessTasks();
    }
  }, [params.id]);

  const fetchProcessDetails = async () => {
    try {
      const response: ApiResponse<ProcessDetail> = await apiClient.get(`/api/processes/${params.id}`);
      setProcess(response.data);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch process details');
    } finally {
      setLoading(false);
    }
  };

  const fetchProcessTasks = async () => {
    try {
      const response: ApiResponse<Task[]> = await apiClient.get(`/api/processes/${params.id}/tasks`);
      setTasks(response.data || []);
    } catch (err: any) {
      console.error('Failed to fetch tasks:', err);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-600">Loading process details...</div>
      </div>
    );
  }

  if (!process) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-600">Process not found</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <button
            onClick={() => router.back()}
            className="text-blue-600 hover:text-blue-900 mb-2"
          >
            ← Back
          </button>
          <h1 className="text-2xl font-bold text-gray-900">Process Instance Details</h1>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Process Information */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Process Information</h2>
            <dl className="space-y-4">
              <div>
                <dt className="text-sm font-medium text-gray-500">Process Definition</dt>
                <dd className="mt-1 text-sm text-gray-900">{process.processDefinitionKey}</dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">Process Instance ID</dt>
                <dd className="mt-1 text-sm text-gray-900">{process.processInstanceId}</dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">Business Key</dt>
                <dd className="mt-1 text-sm text-gray-900">{process.businessKey || '-'}</dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">Status</dt>
                <dd className="mt-1">
                  {process.suspended ? (
                    <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">
                      Suspended
                    </span>
                  ) : (
                    <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                      Active
                    </span>
                  )}
                </dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">Started At</dt>
                <dd className="mt-1 text-sm text-gray-900">
                  {process.startTime ? new Date(process.startTime).toLocaleString() : '-'}
                </dd>
              </div>
            </dl>
          </div>

          {/* Process Variables */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Process Variables</h2>
            {process.variables && Object.keys(process.variables).length > 0 ? (
              <dl className="space-y-3">
                {Object.entries(process.variables).map(([key, value]) => (
                  <div key={key} className="border-b border-gray-200 pb-2">
                    <dt className="text-sm font-medium text-gray-500">{key}</dt>
                    <dd className="mt-1 text-sm text-gray-900 break-all">
                      {typeof value === 'object' ? JSON.stringify(value, null, 2) : String(value)}
                    </dd>
                  </div>
                ))}
              </dl>
            ) : (
              <p className="text-gray-500">No variables set for this process.</p>
            )}
          </div>
        </div>

        {/* Tasks */}
        <div className="mt-6 bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Active Tasks</h2>
          {tasks.length > 0 ? (
            <div className="space-y-3">
              {tasks.map((task) => (
                <div key={task.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="text-sm font-medium text-gray-900">{task.name}</h3>
                      <p className="text-sm text-gray-500">Assignee: {task.assignee || 'Unassigned'}</p>
                      <p className="text-sm text-gray-500">
                        Created: {task.createTime ? new Date(task.createTime).toLocaleString() : '-'}
                      </p>
                    </div>
                    <button
                      onClick={() => router.push(`/tasks/${task.id}`)}
                      className="bg-blue-600 text-white px-3 py-1 rounded-md text-sm hover:bg-blue-700"
                    >
                      Open Task
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-gray-500">No active tasks for this process.</p>
          )}
        </div>

        {/* Process Diagram Placeholder */}
        <div className="mt-6 bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Process Diagram</h2>
          <div className="bg-gray-100 rounded-lg h-96 flex items-center justify-center">
            <p className="text-gray-500">Process diagram will be rendered here</p>
          </div>
        </div>
      </main>
    </div>
  );
}
