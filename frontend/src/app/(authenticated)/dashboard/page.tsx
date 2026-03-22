import { apiClient } from '@/lib/api';

async function getEfficiency() {
  try {
    const response = await apiClient.get<any>('/api/analytics/efficiency');
    return response.data;
  } catch { return null; }
}

async function getDistribution() {
  try {
    const response = await apiClient.get<any>('/api/analytics/distribution');
    return response.data;
  } catch { return null; }
}

async function getBottlenecks() {
  try {
    const response = await apiClient.get<any>('/api/analytics/bottlenecks');
    return response.data;
  } catch { return null; }
}

async function getSlaCompliance() {
  try {
    const response = await apiClient.get<any>('/api/analytics/sla-compliance');
    return response.data;
  } catch { return null; }
}

export default async function DashboardPage() {
  const [efficiency, distribution, bottlenecks, sla] = await Promise.all([
    getEfficiency(), getDistribution(), getBottlenecks(), getSlaCompliance(),
  ]);

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-bold text-gray-900">Analytics Dashboard</h1>
            <a href="/dashboard/custom" className="px-4 py-2 text-sm font-medium text-blue-600 bg-blue-50 rounded-md hover:bg-blue-100">
              Custom Dashboards
            </a>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Task Efficiency */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Task Completion Efficiency</h2>
            {efficiency ? (
              <div>
                <p className="text-3xl font-bold text-blue-600">{efficiency.totalCompletedTasks}</p>
                <p className="text-sm text-gray-500">Total completed tasks</p>
                {efficiency.avgCompletionTimeMinutes && (
                  <div className="mt-4 space-y-2">
                    {Object.entries(efficiency.avgCompletionTimeMinutes as Record<string, number>).map(([key, value]) => (
                      <div key={key} className="flex justify-between text-sm">
                        <span className="text-gray-600 truncate max-w-[60%]">{key}</span>
                        <span className="font-medium">{(value as number).toFixed(1)} min</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ) : (
              <p className="text-sm text-gray-500">No data available</p>
            )}
          </div>

          {/* Process Distribution */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Process Distribution</h2>
            {distribution ? (
              <div>
                <p className="text-3xl font-bold text-green-600">{distribution.totalInstances}</p>
                <p className="text-sm text-gray-500">Total instances</p>
                {distribution.distribution && (
                  <div className="mt-4 space-y-2">
                    {Object.entries(distribution.distribution as Record<string, number>).map(([key, value]) => (
                      <div key={key} className="flex justify-between text-sm">
                        <span className="text-gray-600">{key}</span>
                        <span className="font-medium">{value as number}</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ) : (
              <p className="text-sm text-gray-500">No data available</p>
            )}
          </div>

          {/* Bottleneck Analysis */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Bottleneck Analysis</h2>
            {bottlenecks && bottlenecks.bottlenecks ? (
              <div className="space-y-3">
                {Object.entries(bottlenecks.bottlenecks as Record<string, number>).slice(0, 5).map(([key, value]) => (
                  <div key={key}>
                    <div className="flex justify-between text-sm mb-1">
                      <span className="text-gray-600">{key}</span>
                      <span className="font-medium text-orange-600">{(value as number).toFixed(1)} min avg</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div className="bg-orange-500 h-2 rounded-full" style={{ width: `${Math.min((value as number) / 10 * 100, 100)}%` }} />
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-gray-500">No bottleneck data available</p>
            )}
          </div>

          {/* SLA Compliance */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">SLA Compliance</h2>
            {sla ? (
              <div className="text-center">
                <p className="text-5xl font-bold text-purple-600">{sla.complianceRate}%</p>
                <p className="text-sm text-gray-500 mt-2">Compliance Rate</p>
                <div className="mt-4 grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-lg font-semibold text-gray-900">{sla.completedOnTime}</p>
                    <p className="text-xs text-gray-500">On Time</p>
                  </div>
                  <div>
                    <p className="text-lg font-semibold text-gray-900">{sla.totalWithDueDate}</p>
                    <p className="text-xs text-gray-500">With Due Date</p>
                  </div>
                </div>
              </div>
            ) : (
              <p className="text-sm text-gray-500">No SLA data available</p>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
