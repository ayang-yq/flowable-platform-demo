'use client';

import { useState, useEffect } from 'react';
import { useParams, useSearchParams, useRouter } from 'next/navigation';
import { ArrowLeft, Clock, User, Key, FileText } from 'lucide-react';
import { apiClient, DiagramData } from '@/lib/api';
import TypeBadge from '@/components/workspace/TypeBadge';
import StatusBadge from '@/components/workspace/StatusBadge';
import { BpmnViewer } from '@/components/diagram';
import { CmmnViewer } from '@/components/diagram';

interface TaskDTO {
  id: string;
  name: string;
  assignee: string | null;
  createTime: string | null;
}

interface InstanceDetailDTO {
  id: string;
  definitionId: string;
  definitionKey: string;
  definitionName: string;
  type: 'BPMN' | 'CMMN' | 'DMN';
  startTime: string | null;
  endTime: string | null;
  duration: number | null;
  startedBy: string | null;
  status: 'ACTIVE' | 'SUSPENDED' | 'COMPLETED' | 'CANCELLED' | 'FAILED';
  businessKey: string | null;
  tenantId: string;
  variables: Record<string, any> | null;
  currentActivities: string[] | null;
  tasks: TaskDTO[] | null;
}

function formatDuration(ms: number): string {
  const seconds = Math.floor(ms / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  if (days > 0) return `${days}d ${hours % 24}h`;
  if (hours > 0) return `${hours}h ${minutes % 60}m`;
  if (minutes > 0) return `${minutes}m`;
  return `${seconds}s`;
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString();
}

export default function InstanceDetailPage() {
  const params = useParams();
  const searchParams = useSearchParams();
  const router = useRouter();
  const id = params.id as string;
  const type = searchParams.get('type') || 'BPMN';

  const [detail, setDetail] = useState<InstanceDetailDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Diagram state
  const [diagramData, setDiagramData] = useState<DiagramData | null>(null);
  const [diagramLoading, setDiagramLoading] = useState(false);
  const [diagramError, setDiagramError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDetail = async () => {
      try {
        const response = await apiClient.get<InstanceDetailDTO>(
          `/api/workspace/instances/${id}?type=${type}`
        );
        setDetail(response.data);
        setError(null);
      } catch {
        setError('Failed to load instance details');
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id, type]);

  // Fetch diagram data when detail is loaded
  useEffect(() => {
    if (!detail || detail.status !== 'ACTIVE') return;

    const fetchDiagram = async () => {
      setDiagramLoading(true);
      setDiagramError(null);
      try {
        let data: DiagramData;
        if (type === 'BPMN') {
          data = await apiClient.getProcessInstanceDiagram(id);
        } else if (type === 'CMMN') {
          data = await apiClient.getCaseInstanceDiagram(id);
        } else {
          return; // DMN doesn't have diagrams
        }
        setDiagramData(data);
      } catch (err) {
        setDiagramError('Failed to load diagram');
        console.error('Diagram fetch error:', err);
      } finally {
        setDiagramLoading(false);
      }
    };

    fetchDiagram();
  }, [detail, type, id]);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="animate-pulse space-y-4">
            <div className="h-8 bg-gray-200 rounded w-1/3" />
            <div className="h-48 bg-gray-200 rounded" />
            <div className="h-48 bg-gray-200 rounded" />
          </div>
        </div>
      </div>
    );
  }

  if (error || !detail) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="bg-red-50 border border-red-200 rounded-md p-4 text-red-700">
            {error || 'Instance not found'}
          </div>
          <button
            onClick={() => router.push('/workspace')}
            className="mt-4 text-blue-600 hover:text-blue-800"
          >
            Back to Workspace
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center gap-4">
            <button
              onClick={() => router.push('/workspace')}
              className="text-gray-500 hover:text-gray-700"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <div className="flex items-center gap-3">
              <h1 className="text-2xl font-bold text-gray-900">
                {detail.definitionName || detail.definitionKey}
              </h1>
              <TypeBadge type={detail.type} />
              <StatusBadge status={detail.status} />
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* Metadata card */}
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Instance Details</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <div className="flex items-start gap-2">
              <Key className="w-4 h-4 text-gray-400 mt-0.5" />
              <div>
                <p className="text-xs text-gray-500">Instance ID</p>
                <p className="text-sm font-mono text-gray-900">{detail.id}</p>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <User className="w-4 h-4 text-gray-400 mt-0.5" />
              <div>
                <p className="text-xs text-gray-500">Started By</p>
                <p className="text-sm text-gray-900">{detail.startedBy || '-'}</p>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <Clock className="w-4 h-4 text-gray-400 mt-0.5" />
              <div>
                <p className="text-xs text-gray-500">Start Time</p>
                <p className="text-sm text-gray-900">{formatDate(detail.startTime)}</p>
              </div>
            </div>
            {detail.endTime && (
              <div className="flex items-start gap-2">
                <Clock className="w-4 h-4 text-gray-400 mt-0.5" />
                <div>
                  <p className="text-xs text-gray-500">End Time</p>
                  <p className="text-sm text-gray-900">{formatDate(detail.endTime)}</p>
                </div>
              </div>
            )}
            {detail.duration != null && (
              <div className="flex items-start gap-2">
                <Clock className="w-4 h-4 text-gray-400 mt-0.5" />
                <div>
                  <p className="text-xs text-gray-500">Duration</p>
                  <p className="text-sm text-gray-900">{formatDuration(detail.duration)}</p>
                </div>
              </div>
            )}
            {detail.businessKey && (
              <div className="flex items-start gap-2">
                <FileText className="w-4 h-4 text-gray-400 mt-0.5" />
                <div>
                  <p className="text-xs text-gray-500">Business Key</p>
                  <p className="text-sm text-gray-900">{detail.businessKey}</p>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Diagram Viewer */}
        {detail.status === 'ACTIVE' && (
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              {type === 'BPMN' ? 'Process Diagram' : 'Case Diagram'}
            </h2>
            {diagramLoading && (
              <div className="flex items-center justify-center py-12">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                <span className="ml-3 text-sm text-gray-600">Loading diagram...</span>
              </div>
            )}
            {diagramError && (
              <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4 text-yellow-700">
                {diagramError}
              </div>
            )}
            {!diagramLoading && !diagramError && diagramData && (
              <div className="border border-gray-200 rounded-lg overflow-hidden">
                {type === 'BPMN' && (
                  <BpmnViewer
                    xml={diagramData.diagramXml}
                    activeElementIds={diagramData.activeElementIds}
                    completedElementIds={diagramData.completedElementIds}
                    currentElementId={diagramData.currentElementId || ''}
                  />
                )}
                {type === 'CMMN' && (
                  <CmmnViewer
                    xml={diagramData.diagramXml}
                    activeElementIds={diagramData.activeElementIds}
                    completedElementIds={diagramData.completedElementIds}
                    currentElementId={diagramData.currentElementId || ''}
                  />
                )}
              </div>
            )}
            {!diagramLoading && !diagramError && !diagramData && (
              <div className="text-center py-12 text-gray-500">
                No diagram available for this {type.toLowerCase()} instance
              </div>
            )}
          </div>
        )}

        {/* Current activities */}
        {detail.currentActivities && detail.currentActivities.length > 0 && (
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Current Activities</h2>
            <div className="flex flex-wrap gap-2">
              {detail.currentActivities.map((activity, i) => (
                <span
                  key={i}
                  className="inline-flex items-center px-3 py-1 rounded-full text-sm bg-blue-50 text-blue-700"
                >
                  {activity}
                </span>
              ))}
            </div>
          </div>
        )}

        {/* Active tasks */}
        {detail.tasks && detail.tasks.length > 0 && (
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Active Tasks</h2>
            <div className="space-y-3">
              {detail.tasks.map((task) => (
                <div
                  key={task.id}
                  className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                >
                  <div>
                    <p className="text-sm font-medium text-gray-900">{task.name || 'Unnamed Task'}</p>
                    <p className="text-xs text-gray-500">
                      Assigned to: {task.assignee || 'Unassigned'}
                    </p>
                  </div>
                  <p className="text-xs text-gray-400">{formatDate(task.createTime)}</p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Variables */}
        {detail.variables && Object.keys(detail.variables).length > 0 && (
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Variables</h2>
            <table className="min-w-full divide-y divide-gray-200">
              <thead>
                <tr>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Key</th>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Value</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {Object.entries(detail.variables).map(([key, value]) => (
                  <tr key={key}>
                    <td className="px-4 py-2 text-sm font-mono text-gray-900">{key}</td>
                    <td className="px-4 py-2 text-sm text-gray-700">
                      {typeof value === 'object' ? JSON.stringify(value) : String(value)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  );
}
