import { apiClient } from '@/lib/api';
import { TaskFilters, TaskPageResponse } from '@/types/task';
import TaskListContainer from '@/components/tasks/TaskListContainer';

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
        <div className="flex items-center justify-between mb-4">
          <p className="text-sm text-gray-600">
            Showing {tasksData.content.length} of {tasksData.totalElements} tasks
          </p>
        </div>
        <TaskListContainer initialData={tasksData} />
      </main>
    </div>
  );
}
