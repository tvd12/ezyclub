package org.youngmonkeys.ezyclub.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebEzyclubLessonResponse {
    private long id;
    private long courseId;
    private String title;
    private String content;
    private String videoUrl;
    private long displayOrder;
    private boolean published;
    private boolean completed;
}
