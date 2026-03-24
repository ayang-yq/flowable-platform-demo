package com.flowable.platform.dto;

import java.util.ArrayList;
import java.util.List;

public class DiagramDataDTO {
    private String diagramXml;
    private List<String> activeElementIds;
    private List<String> completedElementIds;
    private String currentElementId;

    public DiagramDataDTO() {
        this.activeElementIds = new ArrayList<>();
        this.completedElementIds = new ArrayList<>();
    }

    public String getDiagramXml() {
        return diagramXml;
    }

    public void setDiagramXml(String diagramXml) {
        this.diagramXml = diagramXml;
    }

    public List<String> getActiveElementIds() {
        return activeElementIds;
    }

    public void setActiveElementIds(List<String> activeElementIds) {
        this.activeElementIds = activeElementIds;
    }

    public List<String> getCompletedElementIds() {
        return completedElementIds;
    }

    public void setCompletedElementIds(List<String> completedElementIds) {
        this.completedElementIds = completedElementIds;
    }

    public String getCurrentElementId() {
        return currentElementId;
    }

    public void setCurrentElementId(String currentElementId) {
        this.currentElementId = currentElementId;
    }
}
