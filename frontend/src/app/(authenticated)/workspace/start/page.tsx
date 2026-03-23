'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeft, Plus, Trash2, Zap } from 'lucide-react';
import { apiClient } from '@/lib/api';
import DefinitionCard from '@/components/workspace/DefinitionCard';

type DefinitionType = 'BPMN' | 'CMMN' | 'DMN';

interface DefinitionDTO {
  id: string;
  key: string;
  name: string;
  version: number;
  category: string | null;
  type: DefinitionType;
  hasStartForm: boolean;
  deploymentTime: string;
}

interface VariableRow {
  key: string;
  value: string;
  type: 'string' | 'number' | 'boolean';
}

interface DecisionResult {
  decisionKey: string;
  decisionName: string;
  outputVariables: Record<string, any>[];
}

const typeFilters: { label: string; value: string }[] = [
  { label: 'All', value: 'all' },
  { label: 'BPMN', value: 'BPMN' },
  { label: 'CMMN', value: 'CMMN' },
  { label: 'DMN', value: 'DMN' },
];

function convertVariable(row: VariableRow): any {
  if (row.type === 'number') return Number(row.value);
  if (row.type === 'boolean') return row.value.toLowerCase() === 'true';
  return row.value;
}

export default function StartInstancePage() {
  const router = useRouter();
  const [definitions, setDefinitions] = useState<DefinitionDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [typeFilter, setTypeFilter] = useState('all');
  const [error, setError] = useState<string | null>(null);

  // Modal state
  const [selectedDef, setSelectedDef] = useState<DefinitionDTO | null>(null);
  const [variables, setVariables] = useState<VariableRow[]>([]);
  const [businessKey, setBusinessKey] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // DMN result state
  const [decisionResult, setDecisionResult] = useState<DecisionResult | null>(null);

  const fetchDefinitions = useCallback(async () => {
    try {
      const response = await apiClient.get<DefinitionDTO[]>(
        `/api/workspace/definitions?type=${typeFilter}`
      );
      setDefinitions(response.data || []);
      setError(null);
    } catch {
      setError('Failed to load definitions');
    } finally {
      setLoading(false);
    }
  }, [typeFilter]);

  useEffect(() => {
    fetchDefinitions();
  }, [fetchDefinitions]);

  const handleStart = (def: DefinitionDTO) => {
    setSelectedDef(def);
    setVariables([]);
    setBusinessKey('');
    setDecisionResult(null);
  };

  const addVariable = () => {
    setVariables([...variables, { key: '', value: '', type: 'string' }]);
  };

  const removeVariable = (index: number) => {
    setVariables(variables.filter((_, i) => i !== index));
  };

  const updateVariable = (index: number, field: keyof VariableRow, value: string) => {
    const updated = [...variables];
    updated[index] = { ...updated[index], [field]: value };
    setVariables(updated);
  };

  const handleSubmit = async () => {
    if (!selectedDef) return;
    setSubmitting(true);
    setError(null);

    try {
      const vars: Record<string, any> = {};
      variables.forEach((v) => {
        if (v.key.trim()) {
          vars[v.key.trim()] = convertVariable(v);
        }
      });

      if (selectedDef.type === 'DMN') {
        const response = await apiClient.post<DecisionResult>(
          `/api/workspace/decisions/${selectedDef.key}/execute`,
          { inputVariables: vars }
        );
        setDecisionResult(response.data);
        setSubmitting(false);
        return;
      }

      const endpoint =
        selectedDef.type === 'CMMN'
          ? `/api/workspace/cases/${selectedDef.key}/start`
          : `/api/workspace/processes/${selectedDef.key}/start`;

      await apiClient.post(endpoint, {
        variables: Object.keys(vars).length > 0 ? vars : null,
        businessKey: businessKey.trim() || null,
      });

      router.push('/workspace');
    } catch (err: any) {
      setError(err.message || 'Failed to start instance');
    } finally {
      setSubmitting(false);
    }
  };

  const closeModal = () => {
    setSelectedDef(null);
    setDecisionResult(null);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex items-center gap-4">
          <button
            onClick={() => router.push('/workspace')}
            className="text-gray-500 hover:text-gray-700"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <h1 className="text-2xl font-bold text-gray-900">Start New Instance</h1>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-md text-sm text-red-700">
            {error}
          </div>
        )}

        {/* Type filter tabs */}
        <div className="flex gap-2 mb-6">
          {typeFilters.map((f) => (
            <button
              key={f.value}
              onClick={() => {
                setTypeFilter(f.value);
                setLoading(true);
              }}
              className={`px-4 py-2 text-sm font-medium rounded-md transition-colors ${
                typeFilter === f.value
                  ? 'bg-blue-600 text-white'
                  : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>

        {/* Definitions grid */}
        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {[...Array(8)].map((_, i) => (
              <div key={i} className="bg-white rounded-lg border border-gray-200 p-4 animate-pulse">
                <div className="h-4 bg-gray-100 rounded w-3/4 mb-2" />
                <div className="h-3 bg-gray-100 rounded w-1/2 mb-4" />
                <div className="h-8 bg-gray-100 rounded" />
              </div>
            ))}
          </div>
        ) : definitions.length === 0 ? (
          <div className="bg-white rounded-lg border border-gray-200 p-12 text-center">
            <p className="text-gray-500">No definitions available.</p>
            <p className="text-sm text-gray-400 mt-1">
              Deploy process or case models from the Admin panel.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {definitions.map((def) => (
              <DefinitionCard
                key={def.id}
                id={def.id}
                name={def.name}
                definitionKey={def.key}
                version={def.version}
                category={def.category}
                type={def.type}
                hasStartForm={def.hasStartForm}
                onClick={() => handleStart(def)}
              />
            ))}
          </div>
        )}
      </main>

      {/* Variable input modal */}
      {selectedDef && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-lg max-h-[80vh] overflow-y-auto">
            <div className="p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-1">
                {selectedDef.type === 'DMN' ? 'Execute Decision' : 'Start Instance'}
              </h2>
              <p className="text-sm text-gray-500 mb-4">
                {selectedDef.name || selectedDef.key}
              </p>

              {/* Decision result */}
              {decisionResult && (
                <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-md">
                  <h3 className="text-sm font-medium text-green-800 mb-2">Decision Output</h3>
                  {decisionResult.outputVariables.map((output, i) => (
                    <div key={i} className="text-sm text-green-700">
                      {Object.entries(output).map(([k, v]) => (
                        <div key={k} className="flex justify-between py-1">
                          <span className="font-mono">{k}</span>
                          <span className="font-medium">{String(v)}</span>
                        </div>
                      ))}
                    </div>
                  ))}
                  <button
                    onClick={() => {
                      setDecisionResult(null);
                      setVariables([]);
                    }}
                    className="mt-3 flex items-center gap-1 text-sm text-green-700 hover:text-green-900"
                  >
                    <Zap className="w-3.5 h-3.5" /> Execute Another
                  </button>
                </div>
              )}

              {!decisionResult && (
                <>
                  {/* Business key (not for DMN) */}
                  {selectedDef.type !== 'DMN' && (
                    <div className="mb-4">
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Business Key (optional)
                      </label>
                      <input
                        type="text"
                        value={businessKey}
                        onChange={(e) => setBusinessKey(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="e.g., REQ-2026-001"
                      />
                    </div>
                  )}

                  {/* Variables */}
                  <div className="mb-4">
                    <div className="flex items-center justify-between mb-2">
                      <label className="text-sm font-medium text-gray-700">
                        {selectedDef.type === 'DMN' ? 'Input Variables' : 'Variables'}
                      </label>
                      <button
                        onClick={addVariable}
                        className="flex items-center gap-1 text-xs text-blue-600 hover:text-blue-800"
                      >
                        <Plus className="w-3.5 h-3.5" /> Add
                      </button>
                    </div>

                    {variables.length === 0 && (
                      <p className="text-xs text-gray-400 mb-2">
                        No variables. Click Add to include process variables.
                      </p>
                    )}

                    <div className="space-y-2">
                      {variables.map((v, i) => (
                        <div key={i} className="flex gap-2 items-start">
                          <input
                            type="text"
                            value={v.key}
                            onChange={(e) => updateVariable(i, 'key', e.target.value)}
                            className="flex-1 px-2 py-1.5 border border-gray-300 rounded text-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
                            placeholder="Key"
                          />
                          <input
                            type="text"
                            value={v.value}
                            onChange={(e) => updateVariable(i, 'value', e.target.value)}
                            className="flex-1 px-2 py-1.5 border border-gray-300 rounded text-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
                            placeholder="Value"
                          />
                          <select
                            value={v.type}
                            onChange={(e) => updateVariable(i, 'type', e.target.value)}
                            className="px-2 py-1.5 border border-gray-300 rounded text-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
                          >
                            <option value="string">String</option>
                            <option value="number">Number</option>
                            <option value="boolean">Boolean</option>
                          </select>
                          <button
                            onClick={() => removeVariable(i)}
                            className="p-1.5 text-gray-400 hover:text-red-600"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>

                  {error && (
                    <div className="mb-4 p-2 bg-red-50 border border-red-200 rounded text-xs text-red-700">
                      {error}
                    </div>
                  )}
                </>
              )}

              <div className="flex justify-end gap-3 pt-2">
                <button
                  onClick={closeModal}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                >
                  {decisionResult ? 'Close' : 'Cancel'}
                </button>
                {!decisionResult && (
                  <button
                    onClick={handleSubmit}
                    disabled={submitting}
                    className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:opacity-50"
                  >
                    {submitting
                      ? 'Processing...'
                      : selectedDef.type === 'DMN'
                      ? 'Execute'
                      : 'Start'}
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
