package com.flowable.platform.dto;

import java.util.List;

public class HomeSummaryDTO {
    private long pendingTaskCount;
    private long activeProcessCount;
    private List<ActivityItemDTO> recentActivity;

    public HomeSummaryDTO() {}

    public HomeSummaryDTO(long pendingTaskCount, long activeProcessCount, List<ActivityItemDTO> recentActivity) {
        this.pendingTaskCount = pendingTaskCount;
        this.activeProcessCount = activeProcessCount;
        this.recentActivity = recentActivity;
    }

    public long getPendingTaskCount() {
        return pendingTaskCount;
    }

    public void setPendingTaskCount(long pendingTaskCount) {
        this.pendingTaskCount = pendingTaskCount;
    }

    public long getActiveProcessCount() {
        return activeProcessCount;
    }

    public void setActiveProcessCount(long activeProcessCount) {
        this.activeProcessCount = activeProcessCount;
    }

    public List<ActivityItemDTO> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<ActivityItemDTO> recentActivity) {
        this.recentActivity = recentActivity;
    }
}
