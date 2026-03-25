'use client';

import React, { useEffect, useRef, useState } from 'react';
import Modeler from 'bpmn-js/lib/Modeler';
import { ZoomIn, ZoomOut, Maximize } from 'lucide-react';

interface BpmnViewerProps {
  xml?: string;
  activeElementIds?: string[];
  completedElementIds?: string[];
  currentElementId?: string;
  loading?: boolean;
  error?: string;
  className?: string;
}

export function BpmnViewer({
  xml = '',
  activeElementIds = [],
  completedElementIds = [],
  currentElementId = '',
  loading = false,
  error = '',
  className = ''
}: BpmnViewerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const modelerRef = useRef<Modeler | null>(null);
  const [zoom, setZoom] = useState(1);

  // Initialize BPMN modeler
  useEffect(() => {
    if (!containerRef.current || !xml) return;

    console.log('BpmnViewer: Initializing with XML length:', xml.length);
    console.log('BpmnViewer: Active elements:', activeElementIds);
    console.log('BpmnViewer: Completed elements:', completedElementIds);

    const modeler = new Modeler({
      container: containerRef.current,
      keyboard: {
        bindTo: document
      }
    });

    modelerRef.current = modeler;

    // Import BPMN XML
    modeler.importXML(xml).then(() => {
      console.log('BpmnViewer: XML imported successfully');
      // Apply overlays for active and completed elements
      applyOverlays(modeler, activeElementIds, completedElementIds, currentElementId);

      // Fit to viewport
      const canvas = modeler.get('canvas') as any;
      canvas.zoom('fit-viewport');
      console.log('BpmnViewer: Diagram rendered and fitted');
    }).catch((err: Error) => {
      console.error('BpmnViewer: Failed to import BPMN diagram:', err);
    });

    return () => {
      if (modelerRef.current) {
        modelerRef.current.destroy();
        modelerRef.current = null;
      }
    };
  }, [xml]);

  // Update overlays when element states change
  useEffect(() => {
    if (!modelerRef.current) return;

    applyOverlays(modelerRef.current, activeElementIds, completedElementIds, currentElementId);
  }, [activeElementIds, completedElementIds, currentElementId]);

  const handleZoomIn = () => {
    const newZoom = Math.min(zoom + 0.25, 3);
    setZoom(newZoom);
    if (modelerRef.current) {
      (modelerRef.current.get('canvas') as any).zoom('fit-viewport', newZoom);
    }
  };

  const handleZoomOut = () => {
    const newZoom = Math.max(zoom - 0.25, 0.5);
    setZoom(newZoom);
    if (modelerRef.current) {
      (modelerRef.current.get('canvas') as any).zoom('fit-viewport', newZoom);
    }
  };

  const handleFitToScreen = () => {
    setZoom(1);
    if (modelerRef.current) {
      (modelerRef.current.get('canvas') as any).zoom('fit-viewport');
    }
  };

  return (
    <div className={`diagram-viewer relative ${className}`}>
      {/* Loading State */}
      {loading && (
        <div className="absolute inset-0 flex items-center justify-center bg-gray-50 z-10">
          <div className="text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
            <p className="mt-2 text-sm text-gray-600">Loading diagram...</p>
          </div>
        </div>
      )}

      {/* Error State */}
      {error && !loading && (
        <div className="absolute inset-0 flex items-center justify-center bg-gray-50 z-10">
          <div className="text-center">
            <p className="text-red-600 font-medium">Unable to load diagram</p>
            <p className="text-sm text-gray-600 mt-1">{error}</p>
          </div>
        </div>
      )}

      {/* Zoom Controls */}
      {!loading && !error && (
        <div className="absolute top-4 right-4 z-10 flex gap-2">
          <button
            onClick={handleZoomOut}
            className="p-2 bg-white border border-gray-300 rounded hover:bg-gray-50 transition-colors"
            title="Zoom out"
            disabled={zoom <= 0.5}
          >
            <ZoomOut className="w-4 h-4" />
          </button>
          <span className="p-2 bg-white border border-gray-300 rounded text-sm font-medium">
            {Math.round(zoom * 100)}%
          </span>
          <button
            onClick={handleZoomIn}
            className="p-2 bg-white border border-gray-300 rounded hover:bg-gray-50 transition-colors"
            title="Zoom in"
            disabled={zoom >= 3}
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
        style={{ width: '100%', height: '400px', backgroundColor: '#ffffff' }}
      />
    </div>
  );
}

function applyOverlays(
  modeler: Modeler,
  activeElementIds: string[],
  completedElementIds: string[],
  currentElementId: string
) {
  const overlays = modeler.get('overlays') as any;
  const elementRegistry = modeler.get('elementRegistry') as any;

  // Clear existing overlays
  overlays.clear();

  // Apply orange overlay for active elements
  activeElementIds.forEach(elementId => {
    const element = elementRegistry.get(elementId);
    if (element) {
      overlays.add(elementId, {
        position: {
          top: 0,
          left: 0
        },
        html: '<div style="background-color: rgba(255, 152, 0, 0.2); border: 2px solid #ff9800; width: 100%; height: 100%; position: absolute; top: 0; left: 0; pointer-events: none;"></div>'
      });
    }
  });

  // Apply gray overlay for completed elements
  completedElementIds.forEach(elementId => {
    const element = elementRegistry.get(elementId);
    if (element && !activeElementIds.includes(elementId)) {
      overlays.add(elementId, {
        position: {
          top: 0,
          left: 0
        },
        html: '<div style="background-color: rgba(200, 200, 200, 0.3); width: 100%; height: 100%; position: absolute; top: 0; left: 0; pointer-events: none;"></div>'
      });
    }
  });

  // Apply animated border for current element
  if (currentElementId) {
    const element = elementRegistry.get(currentElementId);
    if (element) {
      overlays.add(currentElementId, {
        position: {
          top: -2,
          left: -2
        },
        html: '<div style="border: 3px solid #2196f3; width: calc(100% + 4px); height: calc(100% + 4px); position: absolute; top: -2px; left: -2px; pointer-events: none; animation: pulse 2s infinite;"></div>'
      });
    }
  }
}
