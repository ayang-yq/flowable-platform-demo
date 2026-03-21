'use client';

import { useEffect, useRef, useState } from 'react';

interface ProcessDiagramProps {
  xml: string;
  currentActivityId?: string;
  className?: string;
}

export default function ProcessDiagram({ xml, currentActivityId, className = '' }: ProcessDiagramProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let viewer: any = null;

    const loadBpmnViewer = async () => {
      try {
        setLoading(true);

        // Dynamically import bpmn-js
        const BpmnJS = (await import('bpmn-js/lib/NavigatedViewer')).default;
        const { default: moddle } = await import('bpmn-moddle');

        viewer = new BpmnJS({
          container: containerRef.current,
          height: 400,
        });

        await viewer.importXML(xml);

        // Get canvas and overlays
        const canvas = viewer.get('canvas');
        const overlays = viewer.get('overlays');

        // Zoom to fit
        canvas.zoom('fit-viewport');

        // Highlight current activity if provided
        if (currentActivityId) {
          overlays.add(currentActivityId, {
            position: {
              bottom: 0,
              right: 0,
            },
            html: '<div style="background: #00ff00; width: 20px; height: 20px; border-radius: 50%;"></div>',
          });
        }

        setLoading(false);
      } catch (err: any) {
        console.error('Failed to load BPMN diagram:', err);
        setError('Failed to load process diagram');
        setLoading(false);
      }
    };

    if (xml && containerRef.current) {
      loadBpmnViewer();
    }

    return () => {
      if (viewer) {
        viewer.destroy();
      }
    };
  }, [xml, currentActivityId]);

  return (
    <div className={`relative ${className}`}>
      {loading && (
        <div className="absolute inset-0 flex items-center justify-center bg-gray-100">
          <div className="text-gray-600">Loading diagram...</div>
        </div>
      )}
      {error && (
        <div className="absolute inset-0 flex items-center justify-center bg-gray-100">
          <div className="text-red-600">{error}</div>
        </div>
      )}
      <div ref={containerRef} className="w-full h-full" style={{ height: '400px' }} />
    </div>
  );
}
