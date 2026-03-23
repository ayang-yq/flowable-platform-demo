'use client';

type InstanceStatus = 'ACTIVE' | 'SUSPENDED' | 'COMPLETED' | 'CANCELLED' | 'FAILED';

const statusConfig: Record<InstanceStatus, { label: string; color: string }> = {
  ACTIVE: { label: 'Active', color: 'bg-green-100 text-green-800' },
  SUSPENDED: { label: 'Suspended', color: 'bg-yellow-100 text-yellow-800' },
  COMPLETED: { label: 'Completed', color: 'bg-blue-100 text-blue-800' },
  CANCELLED: { label: 'Cancelled', color: 'bg-gray-100 text-gray-800' },
  FAILED: { label: 'Failed', color: 'bg-red-100 text-red-800' },
};

interface StatusBadgeProps {
  status: InstanceStatus;
}

export default function StatusBadge({ status }: StatusBadgeProps) {
  const config = statusConfig[status];
  if (!config) return null;

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${config.color}`}>
      {config.label}
    </span>
  );
}
