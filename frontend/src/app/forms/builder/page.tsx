import { apiClient } from '@/lib/api';
import { FormSchema, FormListResponse } from '@/types/form';
import FormBuilder from './components/FormBuilder';

async function getForms(page = 0, size = 20): Promise<FormListResponse> {
  try {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    const response = await apiClient.get<FormListResponse>(`/api/forms/active?${params}`);
    return response.data;
  } catch (error) {
    console.error('Failed to fetch forms:', error);
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

export default async function FormBuilderPage() {
  const formsData = await getForms();

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Form Builder</h1>
              <p className="text-sm text-gray-600 mt-1">
                Create and manage dynamic forms with visual drag-and-drop interface
              </p>
            </div>
            <div className="flex gap-2">
              <a
                href="/forms/builder"
                className="px-4 py-2 text-sm font-medium text-blue-600 bg-blue-50 rounded-md hover:bg-blue-100"
              >
                New Form
              </a>
              <a
                href="/forms"
                className="px-4 py-2 text-sm font-medium text-gray-600 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
              >
                All Forms
              </a>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Form Builder Interface */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Panel - Field Palette */}
          <div className="lg:col-span-1">
            <div className="bg-white shadow rounded-lg p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Field Palette</h2>
              <p className="text-sm text-gray-600 mb-4">
                Drag and drop fields to build your form
              </p>

              <div className="space-y-2">
                <div className="p-3 bg-gray-50 rounded border cursor-move hover:bg-gray-100 transition">
                  <div className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                    <span className="text-sm font-medium text-gray-700">Text Input</span>
                  </div>
                </div>

                <div className="p-3 bg-gray-50 rounded border cursor-move hover:bg-gray-100 transition">
                  <div className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 20l4-16m2 16l4-4M4 20l4-16M9 4H5a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-5" />
                    </svg>
                    <span className="text-sm font-medium text-gray-700">Number Input</span>
                  </div>
                </div>

                <div className="p-3 bg-gray-50 rounded border cursor-move hover:bg-gray-100 transition">
                  <div className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <span className="text-sm font-medium text-gray-700">Date Picker</span>
                  </div>
                </div>

                <div className="p-3 bg-gray-50 rounded border cursor-move hover:bg-gray-100 transition">
                  <div className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                    </svg>
                    <span className="text-sm font-medium text-gray-700">Text Area</span>
                  </div>
                </div>

                <div className="p-3 bg-gray-50 rounded border cursor-move hover:bg-gray-100 transition">
                  <div className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                    <span className="text-sm font-medium text-gray-700">Dropdown</span>
                  </div>
                </div>

                <div className="p-3 bg-gray-50 rounded border cursor-move hover:bg-gray-100 transition">
                  <div className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span className="text-sm font-medium text-gray-700">Radio Buttons</span>
                  </div>
                </div>

                <div className="p-3 bg-gray-50 rounded border cursor-move hover:bg-gray-100 transition">
                  <div className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    <span className="text-sm font-medium text-gray-700">Checkbox</span>
                  </div>
                </div>

                <div className="p-3 bg-gray-50 rounded border cursor-move hover:bg-gray-100 transition">
                  <div className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                    </svg>
                    <span className="text-sm font-medium text-gray-700">File Upload</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Center Panel - Form Canvas */}
          <div className="lg:col-span-2">
            <div className="bg-white shadow rounded-lg p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-lg font-semibold text-gray-900">Form Canvas</h2>
                <div className="flex gap-2">
                  <button className="px-3 py-1.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50">
                    Preview
                  </button>
                  <button className="px-3 py-1.5 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">
                    Save Form
                  </button>
                </div>
              </div>

              {/* Form Builder Component */}
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 min-h-[400px]">
                <div className="text-center text-gray-400">
                  <svg className="mx-auto h-12 w-12 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0h6" />
                  </svg>
                  <p className="text-sm">Drag and drop fields here to build your form</p>
                  <p className="text-xs mt-1">Or click a field type to add it</p>
                </div>
              </div>

              {/* Existing Forms List */}
              <div className="mt-8">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Existing Forms</h3>
                {formsData.content.length === 0 ? (
                  <div className="text-center py-8 text-gray-500">
                    <svg className="mx-auto h-12 w-12 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.707.293H19a2 2 0 012 2v1a2 2 0 01-2 2h-2" />
                    </svg>
                    <p className="text-sm">No forms created yet</p>
                    <p className="text-xs mt-1">Start building your first form above</p>
                  </div>
                ) : (
                  <div className="grid gap-4">
                    {formsData.content.map((form) => (
                      <div key={form.id} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition">
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <h4 className="font-medium text-gray-900">{form.name}</h4>
                            {form.description && (
                              <p className="text-sm text-gray-600 mt-1">{form.description}</p>
                            )}
                            <div className="flex items-center gap-2 mt-2">
                              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                Version {form.version}
                              </span>
                              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                                {form.isActive ? 'Active' : 'Inactive'}
                              </span>
                            </div>
                          </div>
                          <div className="flex gap-2">
                            <a
                              href={`/forms/builder/${form.id}`}
                              className="px-3 py-1.5 text-sm font-medium text-blue-600 bg-blue-50 rounded-md hover:bg-blue-100"
                            >
                              Edit
                            </a>
                            <a
                              href={`/forms/${form.id}/versions`}
                              className="px-3 py-1.5 text-sm font-medium text-gray-600 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                            >
                              Versions
                            </a>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
