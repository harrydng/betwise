package com.betwise.dto;

public class UpdateLessonProgressRequest {

    private Integer currentCheckpoint;
    private Integer completedCheckpoints;

    public Integer getCurrentCheckpoint() {
        return currentCheckpoint;
    }

    public void setCurrentCheckpoint(
            Integer currentCheckpoint) {
        this.currentCheckpoint = currentCheckpoint;
    }

    public Integer getCompletedCheckpoints() {
        return completedCheckpoints;
    }

    public void setCompletedCheckpoints(
            Integer completedCheckpoints) {
        this.completedCheckpoints =
                completedCheckpoints;
    }
}