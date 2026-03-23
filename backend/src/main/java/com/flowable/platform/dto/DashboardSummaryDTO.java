package com.flowable.platform.dto;

public class DashboardSummaryDTO {
    private long activeCount;
    private long completedCount;
    private long startedTodayCount;
    private long myActiveCount;

    public DashboardSummaryDTO() {}

    public DashboardSummaryDTO(long activeCount, long completedCount, long startedTodayCount, long myActiveCount) {
        this.activeCount = activeCount;
        this.completedCount = completedCount;
        this.startedTodayCount = startedTodayCount;
        this.myActiveCount = myActiveCount;
    }

    public long getActiveCount() { return activeCount; }
    public void setActiveCount(long activeCount) { this.activeCount = activeCount; }

    public long getCompletedCount() { return completedCount; }
    public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }

    public long getStartedTodayCount() { return startedTodayCount; }
    public void setStartedTodayCount(long startedTodayCount) { this.startedTodayCount = startedTodayCount; }

    public long getMyActiveCount() { return myActiveCount; }
    public void setMyActiveCount(long myActiveCount) { this.myActiveCount = myActiveCount; }
}
