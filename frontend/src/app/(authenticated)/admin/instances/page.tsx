import { apiClient } from '@/lib/api';

interface ProcessInstanceDTO {
  id: string;
  processDefinitionId: string;
  processDefinitionKey: string;
  processDefinitionName: string;
  startTime: string;
  isSuspended: boolean;
  tenantId: string;
}

async function getInstances(): Promise<ProcessInstanceDTO[]> {
  try {
    const response = await apiClient.get<ProcessInstanceDTO[]>('/api/admin/instances');
    return response.data || [];
  } catch {
    return [];
  }
}

export default async function AdminInstancesPage() {
  const instances = await getInstances();

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <h1 className="text-2xl font-bold text-gray-900">Process Instances</h1>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white shadow rounded-lg overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Instance ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Process</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Start Time</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                <th className="relative px-6 py-3"><span className="sr-only">Actions</span></th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {instances.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-sm text-gray-500">
                    No running process instances.
                  </td>
                </tr>
              ) : (
                instances.map((instance) => (
                  <tr key={instance.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm font-mono text-gray-900">{instance.id.substring(0, 8)}...</td>
                    <td className="px-6 py-4 text-sm text-gray-900">{instance.processDefinitionName}</td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {instance.startTime ? new Date(instance.startTime).toLocaleString() : '-'}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        instance.isSuspended
                          ? 'bg-yellow-100 text-yellow-800'
                          : 'bg-green-100 text-green-800'
                      }`}>
                        {instance.isSuspended ? 'Suspended' : 'Active'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right text-sm font-medium">
                      <a href={`/admin/instances/${instance.id}`} className="text-blue-600 hover:text-blue-900">
                        Manage
                      </a>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
}
