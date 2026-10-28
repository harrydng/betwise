package com.betwise.dto;

import com.betwise.model.LessonCheckpoint;

public record LessonCheckpointResponse(
        Long id,
        int position,
        String title,
        String body
) {

    public LessonCheckpointResponse(LessonCheckpoint checkpoint) {
        this(
                checkpoint.getId(),
                checkpoint.getPosition(),
                checkpoint.getTitle(),
                checkpoint.getBody());
    }
}
