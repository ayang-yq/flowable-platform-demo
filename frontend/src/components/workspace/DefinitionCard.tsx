'use client';

import { Play } from 'lucide-react';
import TypeBadge from './TypeBadge';

interface DefinitionCardProps {
  id: string;
  name: string;
  definitionKey: string;
  version: number;
  category: string | null;
  type: 'BPMN' | 'CMMN' | 'DMN';
  hasStartForm: boolean;
  onClick: () => void;
}

export default function DefinitionCard({
  name,
  definitionKey,
  version,
  category,
  type,
  hasStartForm,
  onClick,
}: DefinitionCardProps) {
  const buttonLabel = type === 'DMN' ? 'Execute' : 'Start';

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4 hover:shadow-md hover:border-gray-300 transition-all">
      <div className="flex items-start justify-between mb-3">
        <div className="flex-1 min-w-0">
          <h3 className="text-sm font-semibold text-gray-900 truncate">
            {name || definitionKey}
          </h3>
          <p className="text-xs text-gray-500 mt-0.5 font-mono">{definitionKey}</p>
        </div>
        <TypeBadge type={type} />
      </div>

      <div className="flex items-center gap-3 text-xs text-gray-500 mb-3">
        <span>v{version}</span>
        {category && <span>{category}</span>}
        {hasStartForm && (
          <span className="text-blue-600">Has form</span>
        )}
      </div>

      <button
        onClick={onClick}
        className="w-full flex items-center justify-center gap-1.5 px-3 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors"
      >
        <Play className="w-3.5 h-3.5" />
        {buttonLabel}
      </button>
    </div>
  );
}
