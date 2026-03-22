'use client';

import { useState } from 'react';

interface InstanceDetailProps {
  params: { id: string };
}

export default function InstanceDetailPage({ params }: InstanceDetailProps) {
  const [variables, setVariables] = useState('{}');
  const [targetNode, setTargetNode] = useState('');
  const [currentNode, setCurrentNode] = useState('');
  const [message, setMessage] = useState<string | null>(null);

  const instanceId = params.id;

  const handleAction = async (action: string) => {
    try {
      const response = await fetch(`/api/admin/instances/${instanceId}/${action}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({}),
      });
      if (response.ok) {
        setMessage(`Successfully ${action}d instance.`);
      } else {
        setMessage(`Failed to ${action} instance.`);
      }
    } catch {
      setMessage(`Error performing ${action}.`);
    }
  };

  const handleModifyVariables = async () => {
    try {
      const vars = JSON.parse(variables);
      const response = await fetch(`/api/admin/instances/${instanceId}/variables`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(vars),
      });
      if (response.ok) {
        setMessage('Variables updated successfully.');
      }
    } catch {
      setMessage('Invalid JSON or update failed.');
    }
  };

  const handleJumpToNode = async () => {
    try {
      const response = await fetch(`/api/admin/instances/${instanceId}/jump`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currentActivityId: currentNode, targetActivityId: targetNode }),
      });
      if (response.ok) {
        setMessage('Node jump successful.');
      }
    } catch {
      setMessage('Node jump failed.');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <h1 className="text-2xl font-bold text-gray-900">Instance: {instanceId.substring(0, 8)}...</h1>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {message && (
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <p className="text-sm text-blue-800">{message}</p>
          </div>
        )}

        {/* Instance Controls */}
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Instance Controls</h2>
          <div className="flex gap-3">
            <button
              onClick={() => handleAction('suspend')}
              className="px-4 py-2 text-sm font-medium text-yellow-700 bg-yellow-100 rounded-md hover:bg-yellow-200"
            >
              Suspend
            </button>
            <button
              onClick={() => handleAction('activate')}
              className="px-4 py-2 text-sm font-medium text-green-700 bg-green-100 rounded-md hover:bg-green-200"
            >
              Activate
            </button>
            <button
              onClick={() => handleAction('terminate')}
              className="px-4 py-2 text-sm font-medium text-red-700 bg-red-100 rounded-md hover:bg-red-200"
            >
              Terminate
            </button>
          </div>
        </div>

        {/* Modify Variables */}
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Modify Variables</h2>
          <textarea
            value={variables}
            onChange={(e) => setVariables(e.target.value)}
            rows={5}
            className="w-full px-3 py-2 border border-gray-300 rounded-md font-mono text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder='{"key": "value"}'
          />
          <button
            onClick={handleModifyVariables}
            className="mt-3 px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700"
          >
            Update Variables
          </button>
        </div>

        {/* Node Jump */}
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Jump to Node</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Current Activity ID</label>
              <input
                type="text"
                value={currentNode}
                onChange={(e) => setCurrentNode(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="e.g., userTask1"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Target Activity ID</label>
              <input
                type="text"
                value={targetNode}
                onChange={(e) => setTargetNode(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="e.g., userTask2"
              />
            </div>
          </div>
          <button
            onClick={handleJumpToNode}
            disabled={!currentNode || !targetNode}
            className="px-4 py-2 text-sm font-medium text-white bg-orange-600 rounded-md hover:bg-orange-700 disabled:opacity-50"
          >
            Execute Jump
          </button>
        </div>
      </main>
    </div>
  );
}
