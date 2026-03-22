import { apiClient } from '@/lib/api';

interface DepartmentDTO {
  id: string;
  name: string;
  code: string;
  parentId: string | null;
  parentName: string | null;
  managerId: string | null;
  managerName: string | null;
  path: string;
  level: number;
  isActive: boolean;
  createdAt: string;
  children: DepartmentDTO[] | null;
}

async function getDepartments(): Promise<DepartmentDTO[]> {
  try {
    const response = await apiClient.get<DepartmentDTO[]>('/api/departments');
    return response.data || [];
  } catch {
    return [];
  }
}

function DepartmentNode({ dept, depth = 0 }: { dept: DepartmentDTO; depth?: number }) {
  return (
    <>
      <tr className="hover:bg-gray-50">
        <td className="px-6 py-4 whitespace-nowrap">
          <div style={{ paddingLeft: `${depth * 24}px` }} className="flex items-center">
            {depth > 0 && (
              <svg className="w-4 h-4 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            )}
            <div>
              <div className="text-sm font-medium text-gray-900">{dept.name}</div>
              <div className="text-xs text-gray-500">{dept.code}</div>
            </div>
          </div>
        </td>
        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
          {dept.managerName || '-'}
        </td>
        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
          {dept.path}
        </td>
        <td className="px-6 py-4 whitespace-nowrap">
          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
            Level {dept.level}
          </span>
        </td>
        <td className="px-6 py-4 whitespace-nowrap">
          <span
            className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
              dept.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
            }`}
          >
            {dept.isActive ? 'Active' : 'Inactive'}
          </span>
        </td>
        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
          <a href={`/admin/departments/${dept.id}`} className="text-blue-600 hover:text-blue-900">
            Edit
          </a>
        </td>
      </tr>
      {dept.children?.map((child) => (
        <DepartmentNode key={child.id} dept={child} depth={depth + 1} />
      ))}
    </>
  );
}

export default async function DepartmentManagementPage() {
  const departments = await getDepartments();

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-bold text-gray-900">Department Management</h1>
            <a
              href="/admin/departments/new"
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700"
            >
              Add Department
            </a>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white shadow rounded-lg overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Department
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Manager
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Path
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Level
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="relative px-6 py-3">
                  <span className="sr-only">Actions</span>
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {departments.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-sm text-gray-500">
                    No departments found. Click &quot;Add Department&quot; to create one.
                  </td>
                </tr>
              ) : (
                departments.map((dept) => (
                  <DepartmentNode key={dept.id} dept={dept} />
                ))
              )}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
}
