/**
 * Diagram data response from the backend API
 */
export interface DiagramData {
  diagramXml: string;
  activeElementIds: string[];
  completedElementIds: string[];
  currentElementId: string | null;
}

/**
 * API response wrapper for diagram data
 */
export interface DiagramDataResponse {
  diagramXml: string;
  activeElementIds: string[];
  completedElementIds: string[];
  currentElementId: string | null;
}
