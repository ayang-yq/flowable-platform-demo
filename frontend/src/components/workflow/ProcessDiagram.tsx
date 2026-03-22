'use client';

import { useEffect, useRef, useState } from 'react';

interface ProcessDiagramProps {
  processInstanceId?: string;
  xml?: string;
  svg?: string;
  currentActivityIds?: string[];
  className?: string;
}

/**
 * ProcessDiagram Component
 *
 * SECURITY NOTE: When using dangerouslySetInnerHTML with SVG from API,
 * ensure the SVG is sanitized in production using DOMPurify:
 * import DOMPurify from 'dompurify';
 * const cleanSvg = DOMPurify.sanitize(diagramSvg);
 */
export default function ProcessDiagram({
  processInstanceId,
  xml,
  svg,
  currentActivityIds = [],
  className = ''
}: ProcessDiagramProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [diagramSvg, setDiagramSvg] = useState(svg);

  useEffect(() => {
    let viewer: any = null;

    const loadBpmnViewer = async () => {
      try {
        setLoading(true);

        // If SVG is provided directly, use it
        if (diagramSvg) {
          setLoading(false);
          return;
        }

        // If processInstanceId is provided, fetch SVG from backend
        if (processInstanceId && !xml) {
          try {
            const response = await fetch(`/api/processes/${processInstanceId}/diagram`);
            if (!response.ok) throw new Error('Failed to fetch diagram');
            const data = await response.json();
            setDiagramSvg(data.data);
            setLoading(false);
            return;
          } catch (err) {
            console.error('Failed to fetch diagram:', err);
            throw err;
          }
        }

        // Otherwise, use bpmn-js to render from XML
        if (!xml) {
          throw new Error('Either processInstanceId, xml, or svg must be provided');
        }

        // Dynamically import bpmn-js
        const BpmnJS = (await import('bpmn-js/lib/NavigatedViewer')).default;

        viewer = new BpmnJS({
          container: containerRef.current!,
          height: 500,
          width: '100%',
        });

        await viewer.importXML(xml);

        // Get canvas and overlays
        const canvas = viewer.get('canvas');
        const overlays = viewer.get('overlays');
        const elementRegistry = viewer.get('elementRegistry');

        // Zoom to fit
        canvas.zoom('fit-viewport');

        // Highlight current activities (multiple nodes can be active)
        if (currentActivityIds && currentActivityIds.length > 0) {
          currentActivityIds.forEach(activityId => {
            const element = elementRegistry.get(activityId);
            if (element) {
              // Add overlay to highlight the current node
              overlays.add(activityId, {
                position: {
                  bottom: -5,
                  right: -5,
                },
                html: '<div style="background: #22c55e; width: 24px; height: 24px; border-radius: 50%; border: 3px solid #fff; box-shadow: 0 2px 4px rgba(0,0,0,0.2);"></div>',
              });

              // Add color to the element
              canvas.addMarker(activityId, 'current-node');
            }
          });
        }

        // Add custom CSS for the current node marker
        const styleElement = document.createElement('style');
        styleElement.textContent = `
          .current-node .djs-element {
            fill: #dcfce7 !important;
            stroke: #22c55e !important;
            stroke-width: 3px !important;
          }
          .current-node text {
            fill: #15803d !important;
          }
        `;
        document.head.appendChild(styleElement);

        setLoading(false);
      } catch (err: any) {
        console.error('Failed to load BPMN diagram:', err);
        setError('Failed to load process diagram');
        setLoading(false);
      }
    };

    loadBpmnViewer();

    return () => {
      if (viewer) {
        viewer.destroy();
      }
    };
  }, [processInstanceId, xml, diagramSvg, currentActivityIds]);

  // If SVG is available (either from props or fetched), render it
  if (diagramSvg) {
    return (
      <div className={`relative ${className}`}>
        {loading && (
          <div className="absolute inset-0 flex items-center justify-center bg-gray-100 z-10">
            <div className="text-gray-600">Loading diagram...</div>
          </div>
        )}
        {error && (
          <div className="absolute inset-0 flex items-center justify-center bg-gray-100 z-10">
            <div className="text-red-600">{error}</div>
          </div>
        )}
        {/* SECURITY: In production, sanitize with DOMPurify before rendering */}
        <div
          dangerouslySetInnerHTML={{ __html: diagramSvg }}
          className="w-full h-full overflow-auto bg-white rounded-lg"
          style={{ height: '500px' }}
        />
      </div>
    );
  }

  // Otherwise, use bpmn-js viewer
  return (
    <div className={`relative ${className}`}>
      {loading && (
        <div className="absolute inset-0 flex items-center justify-center bg-gray-100 z-10">
          <div className="text-gray-600">Loading diagram...</div>
        </div>
      )}
      {error && (
        <div className="absolute inset-0 flex items-center justify-center bg-gray-100 z-10">
          <div className="text-red-600">{error}</div>
        </div>
      )}
      <div ref={containerRef} className="w-full h-full bg-white rounded-lg" style={{ height: '500px' }} />
    </div>
  );
}
