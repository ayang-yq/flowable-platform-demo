'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Plus, Search, Filter } from 'lucide-react';
import { apiClient } from '@/lib/api';
import InstanceTable from '@/components/workspace/InstanceTable';
import SummaryCards from '@/components/workspace/SummaryCards';

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

interface InstancePageDTO {
  content: InstanceDTO[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

interface DashboardSummary {
  activeCount: number;
  completedCount: number;
  startedTodayCount: number;
  myActiveCount: number;
}

const POLL_INTERVAL = 12000;

export default function WorkspacePage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const initialTab = searchParams.get('tab') === 'completed' ? 'completed' : 'active';

  const [tab, setTab] = useState<'active' | 'completed'>(initialTab);
  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');
  const [myInstances, setMyInstances] = useState(false);

  const [data, setData] = useState<InstancePageDTO | null>(null);
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [summaryLoading, setSummaryLoading] = useState(true);

  const fetchInstances = useCallback(async () => {
    try {
      const params = new URLSearchParams({
        page: String(page),
        size: String(pageSize),
        type: typeFilter,
      });
      if (search) params.set('search', search);
      if (myInstances) {
        const username = localStorage.getItem('username');
        if (username) params.set('startedBy', username);
      }

      const endpoint =
        tab === 'active'
          ? `/api/workspace/instances/active?${params}`
          : `/api/workspace/instances/completed?${params}${statusFilter !== 'all' ? `&status=${statusFilter}` : ''}`;

      const response = await apiClient.get<InstancePageDTO>(endpoint);
      setData(response.data);
    } catch {
      // Keep existing data on poll failure
    } finally {
      setLoading(false);
    }
  }, [tab, page, pageSize, typeFilter, search, myInstances, statusFilter]);

  const fetchSummary = useCallback(async () => {
    try {
      const response = await apiClient.get<DashboardSummary>('/api/workspace/summary');
      setSummary(response.data);
    } catch {
      // Keep existing summary on poll failure
    } finally {
      setSummaryLoading(false);
    }
  }, []);

  // Initial fetch
  useEffect(() => {
    setLoading(true);
    fetchInstances();
    fetchSummary();
  }, [fetchInstances, fetchSummary]);

  // Polling
  useEffect(() => {
    const interval = setInterval(() => {
      fetchInstances();
      fetchSummary();
    }, POLL_INTERVAL);
    return () => clearInterval(interval);
  }, [fetchInstances, fetchSummary]);

  // Reset page on filter change
  useEffect(() => {
    setPage(0);
  }, [tab, typeFilter, statusFilter, myInstances, search]);

  const handleTabChange = (newTab: 'active' | 'completed') => {
    setTab(newTab);
    setLoading(true);
    router.replace(`/workspace?tab=${newTab}`, { scroll: false });
  };

  const pagination = data
    ? {
        totalElements: data.totalElements,
        totalPages: data.totalPages,
        size: data.size,
        number: data.number,
        first: data.first,
        last: data.last,
      }
    : { totalElements: 0, totalPages: 0, size: pageSize, number: 0, first: true, last: true };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-900">Workspace</h1>
          <button
            onClick={() => router.push('/workspace/start')}
            className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors"
          >
            <Plus className="w-4 h-4" />
            Start New
          </button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Summary cards */}
        <SummaryCards summary={summary} loading={summaryLoading} />

        {/* Tabs */}
        <div className="flex gap-1 mb-4 bg-gray-100 p-1 rounded-lg w-fit">
          <button
            onClick={() => handleTabChange('active')}
            className={`px-4 py-2 text-sm font-medium rounded-md transition-colors ${
              tab === 'active'
                ? 'bg-white text-gray-900 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Active
          </button>
          <button
            onClick={() => handleTabChange('completed')}
            className={`px-4 py-2 text-sm font-medium rounded-md transition-colors ${
              tab === 'completed'
                ? 'bg-white text-gray-900 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Completed
          </button>
        </div>

        {/* Filters */}
        <div className="flex flex-wrap items-center gap-3 mb-4">
          {/* Search */}
          <div className="relative flex-1 min-w-[200px] max-w-sm">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by name or business key..."
              className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Type filter */}
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="all">All Types</option>
            <option value="BPMN">BPMN</option>
            <option value="CMMN">CMMN</option>
          </select>

          {/* Status filter (completed tab only) */}
          {tab === 'completed' && (
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="all">All Statuses</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
              <option value="FAILED">Failed</option>
            </select>
          )}

          {/* My Instances toggle */}
          <label className="flex items-center gap-2 text-sm text-gray-700 cursor-pointer">
            <input
              type="checkbox"
              checked={myInstances}
              onChange={(e) => setMyInstances(e.target.checked)}
              className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
            My Instances
          </label>
        </div>

        {/* Instance table */}
        <InstanceTable
          instances={data?.content || []}
          pagination={pagination}
          onPageChange={setPage}
          loading={loading}
          showEndTime={tab === 'completed'}
        />
      </main>
    </div>
  );
}
