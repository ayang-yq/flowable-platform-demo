'use client';

import { useRef } from 'react';

interface ChartData {
  labels: string[];
  values: number[];
}

interface EChartsChartProps {
  type: 'bar' | 'line' | 'pie' | 'funnel';
  title: string;
  data: ChartData;
  height?: number;
}

export default function EChartsChart({ type, title, data, height = 300 }: EChartsChartProps) {
  const chartRef = useRef<HTMLDivElement>(null);

  // Placeholder - in production, use dynamic import of echarts
  return (
    <div className="bg-white rounded-lg p-4">
      <h3 className="text-sm font-medium text-gray-700 mb-2">{title}</h3>
      <div ref={chartRef} style={{ height: `${height}px` }} className="bg-gray-50 rounded flex items-center justify-center">
        <div className="text-center">
          <p className="text-xs text-gray-400 mb-2">{type.toUpperCase()} Chart</p>
          <div className="space-y-1">
            {data.labels.slice(0, 5).map((label, i) => (
              <div key={label} className="flex items-center gap-2 text-xs">
                <div className="w-16 bg-blue-200 rounded" style={{ width: `${Math.max(20, (data.values[i] / Math.max(...data.values)) * 80)}px`, height: '12px' }} />
                <span className="text-gray-600">{label}: {data.values[i]}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
