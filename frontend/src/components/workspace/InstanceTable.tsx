'use client';

import { useState, useCallback, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { ChevronLeft, ChevronRight, ChevronDown, ChevronUp } from 'lucide-react';
import dynamic from 'next/dynamic';
import TypeBadge from './TypeBadge';
import StatusBadge from './StatusBadge';
import { apiClient, DiagramData } from '@/lib/api';

// Lazy load diagram viewers - only loaded when a row is expanded
const BpmnViewer = dynamic(
  () => import('@/components/diagram/BpmnViewer').then(m => ({ default: m.BpmnViewer })),
  { loading: () => <div className="h-48 bg-gray-50 animate-pulse rounded" />, ssr: false }
);
const CmmnViewer = dynamic(
  () => import('@/components/diagram/CmmnViewer').then(m => ({ default: m.CmmnViewer })),
  { loading: () => <div className="h-48 bg-gray-50 animate-pulse rounded" />, ssr: false }
);

interface InstanceDTO {
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
}

interface PaginationMeta {
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

interface InstanceTableProps {
  instances: InstanceDTO[];
  pagination: PaginationMeta;
  onPageChange: (page: number) => void;
  loading?: boolean;
  showEndTime?: boolean;
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

function DiagramPreview({ instance }: { instance: InstanceDTO }) {
  const [diagramData, setDiagramData] = useState<DiagramData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchDiagram = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      let data: DiagramData;
      if (instance.type === 'BPMN') {
        data = await apiClient.getProcessInstanceDiagram(instance.id);
      } else if (instance.type === 'CMMN') {
        data = await apiClient.getCaseInstanceDiagram(instance.id);
      } else {
        setError('Diagram not supported for DMN');
        return;
      }
      setDiagramData(data);
    } catch {
      setError('Failed to load diagram');
    } finally {
      setLoading(false);
    }
  }, [instance.id, instance.type]);

  // Fetch diagram on mount (only when expanded)
  useEffect(() => { fetchDiagram(); }, [fetchDiagram]);

  if (error) {
    return <div className="text-sm text-gray-400 py-4">{error}</div>;
  }

  if (loading) {
    return <div className="h-48 bg-gray-50 animate-pulse rounded" />;
  }

  if (!diagramData || !diagramData.diagramXml) {
    return <div className="text-sm text-gray-400 py-4">No diagram available</div>;
  }

  const commonProps = {
    xml: diagramData.diagramXml,
    activeElementIds: diagramData.activeElementIds || [],
    completedElementIds: diagramData.completedElementIds || [],
    currentElementId: diagramData.currentElementId || '',
  };

  return (
    <div className="max-w-2xl">
      {instance.type === 'BPMN' && <BpmnViewer {...commonProps} />}
      {instance.type === 'CMMN' && <CmmnViewer {...commonProps} />}
    </div>
  );
}

export default function InstanceTable({
  instances,
  pagination,
  onPageChange,
  loading = false,
  showEndTime = false,
}: InstanceTableProps) {
  const router = useRouter();
  const [expandedId, setExpandedId] = useState<string | null>(null);

  const toggleExpand = (e: React.MouseEvent, instanceId: string) => {
    e.stopPropagation();
    setExpandedId(prev => prev === instanceId ? null : instanceId);
  };

  if (loading) {
    return (
      <div className="bg-white shadow rounded-lg p-8">
        <div className="animate-pulse space-y-4">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-12 bg-gray-100 rounded" />
          ))}
        </div>
      </div>
    );
  }

  const colSpan = showEndTime ? 6 : 5;

  return (
    <div className="bg-white shadow rounded-lg overflow-hidden">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="w-8 px-2 py-3"></th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name / Key</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Started By</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Start Time</th>
            {showEndTime && (
              <>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">End Time</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Duration</th>
              </>
            )}
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {instances.length === 0 ? (
            <tr>
              <td colSpan={colSpan} className="px-6 py-12 text-center text-sm text-gray-500">
                No instances found.
              </td>
            </tr>
          ) : (
            instances.map((instance) => (
              <>
                <tr
                  key={instance.id}
                  className="hover:bg-gray-50 cursor-pointer"
                  onClick={() => router.push(`/workspace/${instance.id}?type=${instance.type}`)}
                >
                  <td className="px-2 py-4 text-center">
                    {(instance.type === 'BPMN' || instance.type === 'CMMN') && instance.status === 'ACTIVE' && (
                      <button
                        onClick={(e) => toggleExpand(e, instance.id)}
                        className="p-1 text-gray-400 hover:text-gray-600 rounded"
                        title={expandedId === instance.id ? 'Collapse diagram' : 'Preview diagram'}
                      >
                        {expandedId === instance.id
                          ? <ChevronUp className="w-4 h-4" />
                          : <ChevronDown className="w-4 h-4" />}
                      </button>
                    )}
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm font-medium text-gray-900">
                      {instance.definitionName || instance.definitionKey}
                    </div>
                    {instance.businessKey && (
                      <div className="text-xs text-gray-500 font-mono">{instance.businessKey}</div>
                    )}
                  </td>
                  <td className="px-6 py-4">
                    <TypeBadge type={instance.type} />
                  </td>
                  <td className="px-6 py-4">
                    <StatusBadge status={instance.status} />
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">{instance.startedBy || '-'}</td>
                  <td className="px-6 py-4 text-sm text-gray-500">{formatDate(instance.startTime)}</td>
                  {showEndTime && (
                    <>
                      <td className="px-6 py-4 text-sm text-gray-500">{formatDate(instance.endTime)}</td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        {instance.duration ? formatDuration(instance.duration) : '-'}
                      </td>
                    </>
                  )}
                </tr>
                {expandedId === instance.id && (
                  <tr key={`${instance.id}-diagram`} className="bg-gray-50">
                    <td colSpan={colSpan} className="px-6 py-4">
                      <DiagramPreview instance={instance} />
                    </td>
                  </tr>
                )}
              </>
            ))
          )}
        </tbody>
      </table>

      {pagination.totalPages > 1 && (
        <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
          <div className="text-sm text-gray-700">
            Page {pagination.number + 1} of {pagination.totalPages} ({pagination.totalElements} total)
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => onPageChange(pagination.number - 1)}
              disabled={pagination.first}
              className="inline-flex items-center px-3 py-1.5 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <ChevronLeft className="w-4 h-4 mr-1" />
              Previous
            </button>
            <button
              onClick={() => onPageChange(pagination.number + 1)}
              disabled={pagination.last}
              className="inline-flex items-center px-3 py-1.5 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Next
              <ChevronRight className="w-4 h-4 ml-1" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
