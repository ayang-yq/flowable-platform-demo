import { apiClient } from '@/lib/api';
import { TaskFilters, TaskPageResponse } from '@/types/task';
import TaskCard from '@/components/tasks/TaskCard';

async function getMyTasks(filters: TaskFilters = {}, page = 0, size = 20): Promise<TaskPageResponse> {
  try {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    if (filters.department) params.append('department', filters.department);
    if (filters.priority) params.append('priority', filters.priority);
    if (filters.dueBefore) params.append('dueBefore', filters.dueBefore);

    const response = await apiClient.get<TaskPageResponse>(`/api/tasks/my-tasks?${params}`);
    return response.data;
  } catch (error) {
    console.error('Failed to fetch tasks:', error);
    return {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 20,
      number: 0,
      first: true,
      last: true,
    };
  }
}


export default async function MyTasksPage({
  searchParams,
}: {
  searchParams: { department?: string; priority?: string; dueBefore?: string };
}) {
  const filters: TaskFilters = {
    department: searchParams.department,
    priority: searchParams.priority as any,
    dueBefore: searchParams.dueBefore,
  };

  const tasksData = await getMyTasks(filters);

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-bold text-gray-900">My Tasks</h1>
            <div className="flex gap-2">
              <a
                href="/tasks"
                className="px-4 py-2 text-sm font-medium text-blue-600 bg-blue-50 rounded-md hover:bg-blue-100"
              >
                My Tasks
              </a>
              <a
                href="/tasks/completed"
                className="px-4 py-2 text-sm font-medium text-gray-600 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
              >
                Completed
              </a>
              <a
                href="/tasks/requests"
                className="px-4 py-2 text-sm font-medium text-gray-600 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
              >
                My Requests
              </a>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Filters */}
        <div className="bg-white shadow rounded-lg p-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Filters</h2>
          <form className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label htmlFor="department" className="block text-sm font-medium text-gray-700 mb-1">
                Department
              </label>
              <select
                id="department"
                name="department"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Departments</option>
                <option value="engineering">Engineering</option>
                <option value="finance">Finance</option>
                <option value="hr">Human Resources</option>
                <option value="operations">Operations</option>
              </select>
            </div>

            <div>
              <label htmlFor="priority" className="block text-sm font-medium text-gray-700 mb-1">
                Priority
              </label>
              <select
                id="priority"
                name="priority"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Priorities</option>
                <option value="high">High</option>
                <option value="medium">Medium</option>
                <option value="low">Low</option>
              </select>
            </div>

            <div>
              <label htmlFor="dueBefore" className="block text-sm font-medium text-gray-700 mb-1">
                Due Before
              </label>
              <input
                type="date"
                id="dueBefore"
                name="dueBefore"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="md:col-span-3">
              <button
                type="submit"
                className="w-full md:w-auto px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                Apply Filters
              </button>
            </div>
          </form>
        </div>

        {/* Tasks List */}
        <div className="space-y-4">
          {tasksData.content.length === 0 ? (
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
            <>
              <div className="flex items-center justify-between mb-4">
                <p className="text-sm text-gray-600">
                  Showing {tasksData.content.length} of {tasksData.totalElements} tasks
                </p>
              </div>

              {tasksData.content.map((task) => (
                <TaskCard
                  key={task.id}
                  task={task}
                  showActions={true}
                />
              ))}

              {/* Pagination */}
              {tasksData.totalPages > 1 && (
                <div className="flex items-center justify-between mt-6 pt-6 border-t border-gray-200">
                  <div className="flex-1 flex justify-between sm:hidden">
                    <button
                      disabled={tasksData.first}
                      className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      Previous
                    </button>
                    <button
                      disabled={tasksData.last}
                      className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      Next
                    </button>
                  </div>
                  <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                    <div>
                      <p className="text-sm text-gray-700">
                        Page <span className="font-medium">{tasksData.number + 1}</span> of{' '}
                        <span className="font-medium">{tasksData.totalPages}</span>
                      </p>
                    </div>
                    <div>
                      <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                        <button
                          disabled={tasksData.first}
                          className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          Previous
                        </button>
                        <button
                          disabled={tasksData.last}
                          className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          Next
                        </button>
                      </nav>
                    </div>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </main>
    </div>
  );
}
