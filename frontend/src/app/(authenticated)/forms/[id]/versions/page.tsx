'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { apiClient } from '@/lib/api';
import { FormSchema } from '@/types/form';

interface FormVersionExtended extends FormSchema {
  createdBy?: string;
  description?: string;
}

function parseSchema(schema: string): { fields?: unknown[]; title?: string } | null {
  try {
    return JSON.parse(schema);
  } catch {
    return null;
  }
}

export default function FormVersionsPage() {
  const params = useParams();
  const formId = params.id as string;
  const [form, setForm] = useState<FormSchema | null>(null);
  const [versions, setVersions] = useState<FormVersionExtended[]>([]);

  useEffect(() => {
    async function load() {
      try {
        const [formRes, versionsRes] = await Promise.all([
          apiClient.get<FormSchema>(`/api/forms/${formId}`),
          apiClient.get<FormSchema[]>(`/api/forms/${formId}/versions`),
        ]);
        setForm(formRes.data);
        setVersions((versionsRes.data || []) as FormVersionExtended[]);
      } catch (error) {
        console.error('Failed to fetch form versions:', error);
      }
    }
    load();
  }, [formId]);

  if (!form) return <div className="p-8 text-center text-gray-500">Loading...</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Form Version History</h1>
              <p className="text-sm text-gray-600 mt-1">{form.name}</p>
            </div>
            <div className="flex gap-2">
              <a
                href={`/forms/builder/${formId}`}
                className="px-4 py-2 text-sm font-medium text-blue-600 bg-blue-50 rounded-md hover:bg-blue-100"
              >
                Edit Current
              </a>
              <a
                href="/forms/builder"
                className="px-4 py-2 text-sm font-medium text-gray-600 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
              >
                All Forms
              </a>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Version Timeline */}
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-6">Version Timeline</h2>

          {versions.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              <svg className="mx-auto h-12 w-12 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.707.293H19a2 2 0 012-2v-1a2 2 0 00-2-2h-5" />
              </svg>
              <p className="text-sm">No version history available</p>
            </div>
          ) : (
            <div className="relative">
              {/* Timeline line */}
              <div className="absolute left-4 top-0 bottom-0 w-0.5 bg-gray-200"></div>

              <div className="space-y-6">
                {versions
                  .sort((a, b) => b.version - a.version)
                  .map((version, index, array) => {
                    const isLatest = index === 0;
                    const isOldest = index === array.length - 1;

                    return (
                      <div key={`${version.id}-v${version.version}`} className="relative flex gap-4">
                        {/* Timeline dot */}
                        <div className={`flex-shrink-0 w-8 h-8 rounded-full border-2 flex items-center justify-center ${
                          isLatest
                            ? 'bg-blue-100 border-blue-500'
                            : 'bg-white border-gray-300'
                        }`}>
                          {isLatest && (
                            <svg className="w-4 h-4 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
                              <path d="M16.707 5.293a1 1 0 010-1.414l-8 8a1 1 0 01-1.414 0l-8 8a1 1 0 001.414 0l8-8z" />
                            </svg>
                          )}
                        </div>

                        {/* Version content */}
                        <div className={`flex-1 pb-6 ${!isOldest ? 'border-b border-gray-200' : ''}`}>
                          <div className="flex items-start justify-between mb-2">
                            <div>
                              <div className="flex items-center gap-2 mb-1">
                                <h3 className="text-lg font-semibold text-gray-900">
                                  Version {version.version}
                                </h3>
                                {isLatest && (
                                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                    Latest
                                  </span>
                                )}
                                {version.isActive && (
                                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                                    Active
                                  </span>
                                )}
                              </div>
                              <div className="flex items-center gap-4 text-sm text-gray-600">
                                <div className="flex items-center gap-1">
                                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                                  </svg>
                                  <span>{new Date(version.createdAt).toLocaleDateString()}</span>
                                </div>
                                {version.updatedAt !== version.createdAt && (
                                  <div className="flex items-center gap-1">
                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0l-9 9z" />
                                    </svg>
                                    <span>Updated: {new Date(version.updatedAt).toLocaleDateString()}</span>
                                  </div>
                                )}
                                {version.createdBy && (
                                  <div className="flex items-center gap-1">
                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 007-7z" />
                                    </svg>
                                    <span>Created by: {version.createdBy}</span>
                                  </div>
                                )}
                              </div>
                            </div>
                            <div className="flex gap-2">
                              <button
                                onClick={() => window.open(`/forms/preview/${formId}?version=${version.version}`, '_blank')}
                                className="px-3 py-1.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                              >
                                Preview
                              </button>
                              {isLatest ? (
                                <button
                                  onClick={() => window.open(`/forms/builder/${formId}`, '_blank')}
                                  className="px-3 py-1.5 text-sm font-medium text-blue-600 bg-blue-50 rounded-md hover:bg-blue-100"
                                >
                                  Edit
                                </button>
                              ) : (
                                <button
                                  onClick={() => window.open(`/forms/builder/${formId}?version=${version.version}`, '_blank')}
                                  className="px-3 py-1.5 text-sm font-medium text-gray-600 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                                >
                                  View This Version
                                </button>
                              )}
                            </div>
                          </div>

                          {/* Version details */}
                          <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="bg-gray-50 rounded-lg p-4">
                              <h4 className="text-sm font-medium text-gray-700 mb-2">Schema Summary</h4>
                              <div className="text-sm text-gray-600">
                                {(() => {
                                  const schema = parseSchema(version.schema);
                                  if (!schema) return <p>Unable to parse schema</p>;
                                  return (
                                    <div>
                                      <p>Fields: {schema.fields?.length || 0}</p>
                                      <p>Title: {schema.title || 'N/A'}</p>
                                    </div>
                                  );
                                })()}
                              </div>
                            </div>

                            {version.description && (
                              <div className="bg-gray-50 rounded-lg p-4">
                                <h4 className="text-sm font-medium text-gray-700 mb-2">Description</h4>
                                <p className="text-sm text-gray-600">{version.description}</p>
                              </div>
                            )}
                          </div>

                          {/* Actions for this version */}
                          <div className="mt-4 flex gap-2">
                            <button
                              onClick={() => {
                                navigator.clipboard.writeText(version.schema);
                                alert('Schema copied to clipboard');
                              }}
                              className="px-3 py-1.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                            >
                              Copy Schema JSON
                            </button>
                            <button
                              onClick={() => {
                                const blob = new Blob([version.schema], { type: 'application/json' });
                                const url = URL.createObjectURL(blob);
                                const a = document.createElement('a');
                                a.href = url;
                                a.download = `${form.name}-v${version.version}-schema.json`;
                                a.click();
                                URL.revokeObjectURL(url);
                              }}
                              className="px-3 py-1.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                            >
                              Download Schema
                            </button>
                          </div>
                        </div>
                      </div>
                    );
                  })}
              </div>
            </div>
          )}
        </div>

        {/* Version Comparison */}
        {versions.length >= 2 && (
          <div className="mt-6 bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Version Comparison</h2>
            <p className="text-sm text-gray-600">
              Compare different versions of this form to see what changed between updates.
            </p>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Compare Version</label>
                <select className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                  {versions.map((version) => (
                    <option key={version.version} value={version.version}>
                      Version {version.version}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">With Version</label>
                <select className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                  {versions.map((version) => (
                    <option key={version.version} value={version.version}>
                      Version {version.version}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="mt-4">
              <button
                className="px-4 py-2 text-sm font-medium text-blue-600 bg-blue-50 rounded-md hover:bg-blue-100"
              >
                Compare Versions
              </button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
