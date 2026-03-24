'use client';

import React, { useRef, useEffect, useState, useCallback } from 'react';
import { ZoomIn, ZoomOut, Maximize } from 'lucide-react';

interface DiagramViewerProps {
  xml?: string;
  loading?: boolean;
  error?: string;
  className?: string;
}

interface DiagramViewerState {
  zoom: number;
  loading: boolean;
  error: string | null;
}

export function DiagramViewer({
  xml = '',
  loading = false,
  error = '',
  className = ''
}: DiagramViewerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const viewerRef = useRef<any>(null);
  const [state, setState] = useState<DiagramViewerState>({
    zoom: 1,
    loading: loading,
    error: error || null
  });

  useEffect(() => {
    setState(prev => ({ ...prev, loading, error: error || null }));
  }, [loading, error]);

  const handleZoomIn = useCallback(() => {
    setState(prev => {
      const newZoom = Math.min(prev.zoom + 0.25, 3);
      if (viewerRef.current) {
        viewerRef.current.get('canvas').zoom('fit-viewport', newZoom);
      }
      return { ...prev, zoom: newZoom };
    });
  }, []);

  const handleZoomOut = useCallback(() => {
    setState(prev => {
      const newZoom = Math.max(prev.zoom - 0.25, 0.5);
      if (viewerRef.current) {
        viewerRef.current.get('canvas').zoom('fit-viewport', newZoom);
      }
      return { ...prev, zoom: newZoom };
    });
  }, []);

  const handleFitToScreen = useCallback(() => {
    if (viewerRef.current) {
      viewerRef.current.get('canvas').zoom('fit-viewport');
      setState(prev => ({ ...prev, zoom: 1 }));
    }
  }, []);

  return (
    <div className={`diagram-viewer relative ${className}`}>
      {/* Loading State */}
      {state.loading && (
        <div className="absolute inset-0 flex items-center justify-center bg-gray-50 z-10">
          <div className="text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
            <p className="mt-2 text-sm text-gray-600">Loading diagram...</p>
          </div>
        </div>
      )}

      {/* Error State */}
      {state.error && !state.loading && (
        <div className="absolute inset-0 flex items-center justify-center bg-gray-50 z-10">
          <div className="text-center">
            <p className="text-red-600 font-medium">Unable to load diagram</p>
            <p className="text-sm text-gray-600 mt-1">{state.error}</p>
          </div>
        </div>
      )}

      {/* Zoom Controls */}
      {!state.loading && !state.error && (
        <div className="absolute top-4 right-4 z-10 flex gap-2">
          <button
            onClick={handleZoomOut}
            className="p-2 bg-white border border-gray-300 rounded hover:bg-gray-50 transition-colors"
            title="Zoom out"
            disabled={state.zoom <= 0.5}
          >
            <ZoomOut className="w-4 h-4" />
          </button>
          <span className="p-2 bg-white border border-gray-300 rounded text-sm font-medium">
            {Math.round(state.zoom * 100)}%
          </span>
          <button
            onClick={handleZoomIn}
            className="p-2 bg-white border border-gray-300 rounded hover:bg-gray-50 transition-colors"
            title="Zoom in"
            disabled={state.zoom >= 3}
          >
            <ZoomIn className="w-4 h-4" />
          </button>
          <button
            onClick={handleFitToScreen}
            className="p-2 bg-white border border-gray-300 rounded hover:bg-gray-50 transition-colors"
            title="Fit to screen"
          >
            <Maximize className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Diagram Container */}
      <div
        ref={containerRef}
        className="diagram-container"
        style={{ width: '100%', height: '400px' }}
      />
    </div>
  );
}
