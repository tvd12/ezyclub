package org.youngmonkeys.ezyclub.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LessonModel {
    private long id;
    private long courseId;
    private String title;
    private String content;
    private String videoUrl;
    private long displayOrder;
    private String status;
}
