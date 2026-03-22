'use client';

import { useEffect, useState } from 'react';
import { apiClient, ApiResponse } from '@/lib/api';
import { useRouter } from 'next/navigation';

interface ProcessInstance {
  processInstanceId: string;
  processDefinitionKey: string;
  businessKey: string;
  suspended: boolean;
  startTime: string;
}

export default function ProcessesPage() {
  const router = useRouter();
  const [processes, setProcesses] = useState<ProcessInstance[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchProcesses();
  }, []);

  const fetchProcesses = async () => {
    try {
      const response: ApiResponse<ProcessInstance[]> = await apiClient.get('/api/processes');
      setProcesses(response.data || []);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch processes');
    } finally {
      setLoading(false);
    }
  };

  const handleSuspend = async (id: string) => {
    try {
      await apiClient.post(`/api/processes/${id}/suspend`, {});
      fetchProcesses();
    } catch (err: any) {
      setError(err.message || 'Failed to suspend process');
    }
  };

  const handleActivate = async (id: string) => {
    try {
      await apiClient.post(`/api/processes/${id}/activate`, {});
      fetchProcesses();
    } catch (err: any) {
      setError(err.message || 'Failed to activate process');
    }
  };

  const handleTerminate = async (id: string) => {
    if (confirm('Are you sure you want to terminate this process?')) {
      try {
        await apiClient.post(`/api/processes/${id}/terminate`, {});
        fetchProcesses();
      } catch (err: any) {
        setError(err.message || 'Failed to terminate process');
      }
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-600">Loading processes...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">Process Instances</h1>
          <button
            onClick={() => router.push('/admin/processes/deploy')}
            className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
          >
            Deploy Process
          </button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        <div className="bg-white shadow rounded-lg overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Process Definition
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Business Key
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Started
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {processes.map((process) => (
                <tr key={process.processInstanceId} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <button
                      onClick={() => router.push(`/processes/${process.processInstanceId}`)}
                      className="text-blue-600 hover:text-blue-900"
                    >
                      {process.processDefinitionKey}
                    </button>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-gray-900">
                    {process.businessKey || '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {process.suspended ? (
                      <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">
                        Suspended
                      </span>
                    ) : (
                      <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                        Active
                      </span>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-gray-500">
                    {process.startTime ? new Date(process.startTime).toLocaleString() : '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                    {process.suspended ? (
                      <button
                        onClick={() => handleActivate(process.processInstanceId)}
                        className="text-green-600 hover:text-green-900"
                      >
                        Activate
                      </button>
                    ) : (
                      <button
                        onClick={() => handleSuspend(process.processInstanceId)}
                        className="text-yellow-600 hover:text-yellow-900"
                      >
                        Suspend
                      </button>
                    )}
                    <button
                      onClick={() => handleTerminate(process.processInstanceId)}
                      className="text-red-600 hover:text-red-900"
                    >
                      Terminate
                    </button>
                  </td>
                </tr>
              ))}
              {processes.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-6 py-4 text-center text-gray-500">
                    No process instances found. Start a process or deploy a new process definition.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
}
