package org.youngmonkeys.ezyclub.web.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebEzyclubSaveLessonRequest {
    private String title;
    private String content;
    private String videoUrl;
    private long displayOrder;
    private boolean published;
}
