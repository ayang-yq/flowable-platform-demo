package com.flowable.platform.dto;

import java.util.List;

public class InstancePageDTO {
    private List<InstanceDTO> content;
    private long totalElements;
    private int totalPages;
    private int size;
    private int number;
    private boolean first;
    private boolean last;

    public InstancePageDTO() {}

    public InstancePageDTO(List<InstanceDTO> content, long totalElements, int size, int number) {
        this.content = content;
        this.totalElements = totalElements;
        this.size = size;
        this.number = number;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        this.first = number == 0;
        this.last = number >= totalPages - 1;
    }

    public List<InstanceDTO> getContent() { return content; }
    public void setContent(List<InstanceDTO> content) { this.content = content; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }
}
