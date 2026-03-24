'use client';

import React, { useEffect, useRef, useCallback } from 'react';
import Modeler from 'bpmn-js/lib/Modeler';
import { DiagramViewer } from './DiagramViewer';

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

  // Initialize BPMN modeler
  useEffect(() => {
    if (!containerRef.current || !xml) return;

    const modeler = new Modeler({
      container: containerRef.current,
      keyboard: {
        bindTo: document
      }
    });

    modelerRef.current = modeler;

    // Import BPMN XML
    modeler.importXML(xml).then(() => {
      // Apply overlays for active and completed elements
      applyOverlays(modeler, activeElementIds, completedElementIds, currentElementId);

      // Fit to viewport
      const canvas = modeler.get('canvas') as any;
      canvas.zoom('fit-viewport');
    }).catch((err: Error) => {
      console.error('Failed to import BPMN diagram:', err);
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

  return (
    <DiagramViewer
      xml={xml}
      loading={loading}
      error={error}
      className={className}
    >
      <div ref={containerRef} style={{ width: '100%', height: '100%' }} />
    </DiagramViewer>
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
