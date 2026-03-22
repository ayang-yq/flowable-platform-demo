'use client';

import { useState } from 'react';

interface WidgetConfig {
  id: string;
  type: string;
  title: string;
}

const WIDGET_TYPES = [
  { type: 'task-efficiency', label: 'Task Efficiency' },
  { type: 'process-distribution', label: 'Process Distribution' },
  { type: 'bottleneck-analysis', label: 'Bottleneck Analysis' },
  { type: 'sla-compliance', label: 'SLA Compliance' },
];

export default function CustomDashboardPage() {
  const [dashboardName, setDashboardName] = useState('My Dashboard');
  const [widgets, setWidgets] = useState<WidgetConfig[]>([]);

  const addWidget = (type: string, label: string) => {
    setWidgets([...widgets, { id: `widget-${Date.now()}`, type, title: label }]);
  };

  const removeWidget = (id: string) => {
    setWidgets(widgets.filter((w) => w.id !== id));
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div>
              <input
                type="text"
                value={dashboardName}
                onChange={(e) => setDashboardName(e.target.value)}
                className="text-2xl font-bold text-gray-900 bg-transparent border-none focus:outline-none focus:ring-2 focus:ring-blue-500 rounded px-1"
              />
            </div>
            <div className="flex gap-2">
              <a href="/dashboard" className="px-4 py-2 text-sm font-medium text-gray-600 bg-white border rounded-md hover:bg-gray-50">
                Back
              </a>
              <button className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">
                Save Dashboard
              </button>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Widget Palette */}
        <div className="bg-white shadow rounded-lg p-4 mb-6">
          <h3 className="text-sm font-medium text-gray-700 mb-3">Add Widget</h3>
          <div className="flex flex-wrap gap-2">
            {WIDGET_TYPES.map((wt) => (
              <button
                key={wt.type}
                onClick={() => addWidget(wt.type, wt.label)}
                className="px-3 py-1.5 text-sm border border-gray-300 rounded-md hover:bg-blue-50 hover:border-blue-300"
              >
                + {wt.label}
              </button>
            ))}
          </div>
        </div>

        {/* Dashboard Canvas */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {widgets.length === 0 ? (
            <div className="md:col-span-2 bg-white shadow rounded-lg p-12 text-center">
              <p className="text-gray-500">Add widgets from the palette above to build your dashboard.</p>
            </div>
          ) : (
            widgets.map((widget) => (
              <div key={widget.id} className="bg-white shadow rounded-lg p-6 relative group">
                <button
                  onClick={() => removeWidget(widget.id)}
                  className="absolute top-2 right-2 text-gray-400 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-opacity"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
                <h3 className="text-lg font-semibold text-gray-900">{widget.title}</h3>
                <div className="mt-4 h-40 bg-gray-100 rounded flex items-center justify-center">
                  <p className="text-sm text-gray-400">Chart: {widget.type}</p>
                </div>
              </div>
            ))
          )}
        </div>
      </main>
    </div>
  );
}
