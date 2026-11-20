package org.youngmonkeys.ezyclub.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebEzyclubCourseResponse {
    private long id;
    private String title;
    private String description;
    private int unlockLevel;
    private long displayOrder;
    private boolean published;
    private boolean locked;
    private long lessonCount;
    private long completedLessonCount;
    private int progress;
}
