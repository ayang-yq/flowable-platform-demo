'use client';

import { GitBranch, Layers, Table2 } from 'lucide-react';

type DefinitionType = 'BPMN' | 'CMMN' | 'DMN';

const typeConfig: Record<DefinitionType, { label: string; color: string; icon: typeof GitBranch }> = {
  BPMN: { label: 'BPMN', color: 'bg-blue-100 text-blue-800', icon: GitBranch },
  CMMN: { label: 'CMMN', color: 'bg-green-100 text-green-800', icon: Layers },
  DMN: { label: 'DMN', color: 'bg-orange-100 text-orange-800', icon: Table2 },
};

interface TypeBadgeProps {
  type: DefinitionType;
}

export default function TypeBadge({ type }: TypeBadgeProps) {
  const config = typeConfig[type];
  if (!config) return null;

  const Icon = config.icon;

  return (
    <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium ${config.color}`}>
      <Icon className="w-3 h-3" />
      {config.label}
    </span>
  );
}
