'use client';

import React, { useEffect, useRef, useState } from 'react';
import Modeler from 'cmmn-js/lib/Modeler';
import { ZoomIn, ZoomOut, Maximize } from 'lucide-react';

interface CmmnViewerProps {
  xml?: string;
  activeElementIds?: string[];
  completedElementIds?: string[];
  currentElementId?: string;
  loading?: boolean;
  error?: string;
  className?: string;
}

export function CmmnViewer({
  xml = '',
  activeElementIds = [],
  completedElementIds = [],
  currentElementId = '',
  loading = false,
  error = '',
  className = ''
}: CmmnViewerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const modelerRef = useRef<Modeler | null>(null);
  const [zoom, setZoom] = useState(1);

  // Initialize CMMN modeler
  useEffect(() => {
    if (!containerRef.current || !xml) return;

    console.log('CmmnViewer: Initializing with XML length:', xml.length);
    console.log('CmmnViewer: Active elements:', activeElementIds);
    console.log('CmmnViewer: Completed elements:', completedElementIds);
    console.log('CmmnViewer: XML preview:', xml.substring(0, 200));

    const modeler = new Modeler({
      container: containerRef.current,
      keyboard: {
        bindTo: document
      }
    });

    modelerRef.current = modeler;

    // Import CMMN XML
    modeler.importXML(xml).then(() => {
      console.log('CmmnViewer: XML imported successfully');
      // Apply overlays for active and completed elements
      applyOverlays(modeler, activeElementIds, completedElementIds, currentElementId);

      // Fit to viewport
      const canvas = modeler.get('canvas') as any;
      canvas.zoom('fit-viewport');
      console.log('CmmnViewer: Diagram rendered and fitted');
    }).catch((err: Error) => {
      console.error('CmmnViewer: Failed to import CMMN diagram:', err);
      console.error('CmmnViewer: Error details:', err);
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

  console.log('CmmnViewer: Applying overlays');
  console.log('CmmnViewer: Looking for active elements:', activeElementIds);
  console.log('CmmnViewer: Looking for completed elements:', completedElementIds);

  // Debug: List all available elements in the registry
  const allElements = elementRegistry.getAll();
  console.log('CmmnViewer: All elements in registry:', allElements.map((el: any) => ({ id: el.id, type: el.type, name: el.name })));

  // Apply orange overlay for active plan items
  activeElementIds.forEach(elementId => {
    let element = elementRegistry.get(elementId);
    console.log(`CmmnViewer: Looking for active element "${elementId}":`, element ? 'FOUND' : 'NOT FOUND');

    // If not found by ID, try to find by name or try variations
    if (!element) {
      element = allElements.find((el: any) =>
        el.id === elementId ||
        el.name === elementId ||
        el.id?.includes(elementId) ||
        el.type === elementId
      );
      console.log(`CmmnViewer: Alternative lookup for "${elementId}":`, element ? 'FOUND' : 'NOT FOUND');
    }

    if (element) {
      overlays.add(element.id, {
        position: {
          top: 0,
          left: 0
        },
        html: '<div style="background-color: rgba(255, 152, 0, 0.2); border: 2px solid #ff9800; width: 100%; height: 100%; position: absolute; top: 0; left: 0; pointer-events: none;"></div>'
      });
    }
  });

  // Apply gray overlay for completed plan items
  completedElementIds.forEach(elementId => {
    let element = elementRegistry.get(elementId);
    console.log(`CmmnViewer: Looking for completed element "${elementId}":`, element ? 'FOUND' : 'NOT FOUND');

    if (!element) {
      element = allElements.find((el: any) =>
        el.id === elementId ||
        el.name === elementId ||
        el.id?.includes(elementId) ||
        el.type === elementId
      );
    }

    if (element && !activeElementIds.includes(element.id)) {
      overlays.add(element.id, {
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
    console.log(`CmmnViewer: Looking for current element "${currentElementId}":`, element ? 'FOUND' : 'NOT FOUND');
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
