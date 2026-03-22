import { apiClient } from '@/lib/api';

interface ProcessDefinitionDTO {
  id: string;
  key: string;
  name: string;
  version: number;
  deploymentId: string;
  tenantId: string;
}

async function getProcessDefinitions(): Promise<ProcessDefinitionDTO[]> {
  try {
    const response = await apiClient.get<ProcessDefinitionDTO[]>('/api/admin/processes/definitions');
    return response.data || [];
  } catch {
    return [];
  }
}

export default async function AdminProcessListPage() {
  const definitions = await getProcessDefinitions();

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-bold text-gray-900">Process Definitions</h1>
            <a
              href="/admin/processes/deploy"
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700"
            >
              Deploy New
            </a>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white shadow rounded-lg overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Key</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Version</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tenant</th>
                <th className="relative px-6 py-3"><span className="sr-only">Actions</span></th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {definitions.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-sm text-gray-500">
                    No process definitions deployed.
                  </td>
                </tr>
              ) : (
                definitions.map((def) => (
                  <tr key={def.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm font-medium text-gray-900">{def.name}</td>
                    <td className="px-6 py-4 text-sm text-gray-500 font-mono">{def.key}</td>
                    <td className="px-6 py-4 text-sm">
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
                        v{def.version}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">{def.tenantId || 'Global'}</td>
                    <td className="px-6 py-4 text-right text-sm font-medium">
                      <a href={`/admin/processes/${def.id}`} className="text-blue-600 hover:text-blue-900">View</a>
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
