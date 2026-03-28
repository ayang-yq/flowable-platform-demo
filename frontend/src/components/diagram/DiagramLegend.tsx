'use client';

interface DiagramLegendProps {
  className?: string;
}

export default function DiagramLegend({ className = '' }: DiagramLegendProps) {
  return (
    <div className={`flex items-center gap-4 px-3 py-2 text-xs text-gray-500 bg-gray-50 border-t border-gray-100 rounded-b ${className}`}>
      <div className="flex items-center gap-1.5">
        <span className="w-3 h-3 rounded-full bg-green-500 border-2 border-green-600" />
        <span>Completed</span>
      </div>
      <div className="flex items-center gap-1.5">
        <span className="w-3 h-3 rounded-full bg-orange-500 border-2 border-orange-600" />
        <span>Active</span>
      </div>
      <div className="flex items-center gap-1.5">
        <span className="w-3 h-3 rounded-full bg-blue-500 border-2 border-blue-600" />
        <span>Current</span>
      </div>
    </div>
  );
}
