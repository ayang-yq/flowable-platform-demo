'use client';

import React, { useEffect, useRef, useState } from 'react';
import { ZoomIn, ZoomOut, Maximize } from 'lucide-react';

// Use require for cmmn-js to avoid CommonJS/ES module issues
const CmmnModeler = require('cmmn-js/lib/Modeler');

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
  const modelerRef = useRef<any>(null);
  const [zoom, setZoom] = useState(1);

  // Initialize CMMN modeler
  useEffect(() => {
    if (!containerRef.current || !xml) {
      console.log('CmmnViewer: Skipping initialization - container or XML missing', {
        hasContainer: !!containerRef.current,
        hasXml: !!xml,
        xmlLength: xml?.length
      });
      return;
    }

    console.log('CmmnViewer: ===== INITIALIZATION START =====');
    console.log('CmmnViewer: Container element:', containerRef.current);
    console.log('CmmnViewer: XML length:', xml.length);
    console.log('CmmnViewer: XML preview (first 300 chars):', xml.substring(0, 300));
    console.log('CmmnViewer: Active elements from backend:', activeElementIds);
    console.log('CmmnViewer: Completed elements from backend:', completedElementIds);
    console.log('CmmnViewer: Current element from backend:', currentElementId);

    let modeler: any = null;

    try {
      console.log('CmmnViewer: About to create CmmnModeler...');
      console.log('CmmnModeler constructor:', typeof CmmnModeler, CmmnModeler);

      modeler = new CmmnModeler({
        container: containerRef.current,
        keyboard: {
          bindTo: document
        }
      });

      console.log('CmmnViewer: Modeler created successfully');
      console.log('CmmnViewer: Modeler object:', modeler);
      console.log('CmmnViewer: Modeler methods:', Object.keys(modeler).filter(k => typeof modeler[k] === 'function'));

      modelerRef.current = modeler;

      // Check if importXML is a function
      if (typeof modeler.importXML !== 'function') {
        console.error('CmmnViewer: importXML is not a function on modeler');
        console.error('CmmnViewer: Available methods:', Object.getOwnPropertyNames(Object.getPrototypeOf(modeler)));
        return;
      }

      console.log('CmmnViewer: About to call importXML...');
      // Import CMMN XML
      const importPromise = modeler.importXML(xml);

      console.log('CmmnViewer: importXML returned:', importPromise);
      console.log('CmmnViewer: importPromise type:', typeof importPromise);
      console.log('CmmnViewer: Has then method?', typeof importPromise?.then);

      // Check if importPromise exists and has .then method
      if (!importPromise || typeof importPromise.then !== 'function') {
        console.error('CmmnViewer: importXML did not return a Promise:', importPromise);
        return;
      }

      importPromise.then(() => {
        console.log('CmmnViewer: ===== XML IMPORT SUCCESS =====');

        // Get the canvas to check if diagram was rendered
        const canvas = modeler.get('canvas');
        console.log('CmmnViewer: Canvas object:', canvas);
        console.log('CmmnViewer: Canvas methods:', Object.keys(canvas).filter(k => typeof canvas[k] === 'function'));

        // Get the element registry
        const elementRegistry = modeler.get('elementRegistry');
        console.log('CmmnViewer: ElementRegistry object:', elementRegistry);

        const allElements = elementRegistry.getAll();
        console.log('CmmnViewer: Total elements in registry:', allElements.length);
        console.log('CmmnViewer: All elements:', allElements.map((el: any) => ({ id: el.id, type: el.type, name: el.name })));

        // Apply overlays for active and completed elements
        console.log('CmmnViewer: About to apply overlays...');
        applyOverlays(modeler, activeElementIds, completedElementIds, currentElementId);

        // Fit to viewport
        console.log('CmmnViewer: About to fit to viewport...');
        (canvas as any).zoom('fit-viewport');
        console.log('CmmnViewer: ===== DIAGRAM RENDER COMPLETE =====');
      }).catch((err: Error) => {
        console.error('CmmnViewer: ===== XML IMPORT FAILED =====');
        console.error('CmmnViewer: Failed to import CMMN diagram:', err);
        console.error('CmmnViewer: Error name:', err.name);
        console.error('CmmnViewer: Error message:', err.message);
        console.error('CmmnViewer: Error stack:', err.stack);
      });
    } catch (error) {
      console.error('CmmnViewer: ===== EXCEPTION CATCHED =====');
      console.error('CmmnViewer: Error creating modeler:', error);
      console.error('CmmnViewer: Error details:', error);
    }

    return () => {
      console.log('CmmnViewer: Cleanup - destroying modeler');
      if (modelerRef.current) {
        try {
          modelerRef.current.destroy();
        } catch (e) {
          console.error('CmmnViewer: Error destroying modeler:', e);
        }
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
      {/* Debug Info */}
      <div className="absolute bottom-2 left-2 z-20 bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded">
        CMMN Viewer | XML: {xml ? `${xml.length} chars` : 'none'} | Active: {activeElementIds.length}
      </div>

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
        style={{ width: '100%', height: '400px', backgroundColor: '#ffffff', border: '1px solid #e5e7eb' }}
      />
    </div>
  );
}

function applyOverlays(
  modeler: any,
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
