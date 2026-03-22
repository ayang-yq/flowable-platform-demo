import { apiClient } from '@/lib/api';

interface AuditLogDetail {
  id: string;
  actionType: string;
  entityType: string;
  entityId: string;
  username: string;
  ipAddress: string;
  timestamp: string;
  details: string;
}

async function getAuditLog(id: string): Promise<AuditLogDetail | null> {
  try {
    const response = await apiClient.get<AuditLogDetail>(`/api/admin/audit-logs/${id}`);
    return response.data;
  } catch { return null; }
}

export default async function AuditLogDetailPage({ params }: { params: { id: string } }) {
  const log = await getAuditLog(params.id);

  if (!log) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-gray-500">Audit log entry not found.</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center gap-4">
            <a href="/admin/audit" className="text-blue-600 hover:text-blue-800">&larr; Back</a>
            <h1 className="text-2xl font-bold text-gray-900">Audit Log Detail</h1>
          </div>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white shadow rounded-lg p-6">
          <dl className="space-y-4">
            <div className="grid grid-cols-3 gap-4">
              <dt className="text-sm font-medium text-gray-500">Timestamp</dt>
              <dd className="text-sm text-gray-900 col-span-2">
                {log.timestamp ? new Date(log.timestamp).toLocaleString() : '-'}
              </dd>
            </div>
            <div className="grid grid-cols-3 gap-4 border-t pt-4">
              <dt className="text-sm font-medium text-gray-500">Username</dt>
              <dd className="text-sm text-gray-900 col-span-2">{log.username}</dd>
            </div>
            <div className="grid grid-cols-3 gap-4 border-t pt-4">
              <dt className="text-sm font-medium text-gray-500">Action Type</dt>
              <dd className="col-span-2">
                <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
                  {log.actionType}
                </span>
              </dd>
            </div>
            <div className="grid grid-cols-3 gap-4 border-t pt-4">
              <dt className="text-sm font-medium text-gray-500">Entity Type</dt>
              <dd className="text-sm text-gray-900 col-span-2">{log.entityType}</dd>
            </div>
            <div className="grid grid-cols-3 gap-4 border-t pt-4">
              <dt className="text-sm font-medium text-gray-500">Entity ID</dt>
              <dd className="text-sm text-gray-900 col-span-2 font-mono">{log.entityId}</dd>
            </div>
            <div className="grid grid-cols-3 gap-4 border-t pt-4">
              <dt className="text-sm font-medium text-gray-500">IP Address</dt>
              <dd className="text-sm text-gray-900 col-span-2">{log.ipAddress}</dd>
            </div>
            <div className="grid grid-cols-3 gap-4 border-t pt-4">
              <dt className="text-sm font-medium text-gray-500">Details</dt>
              <dd className="text-sm text-gray-900 col-span-2">
                <pre className="bg-gray-50 rounded p-3 text-xs overflow-x-auto">{log.details}</pre>
              </dd>
            </div>
          </dl>
        </div>
      </main>
    </div>
  );
}
