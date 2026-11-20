package org.youngmonkeys.ezyclub.web.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebEzyclubSaveCourseRequest {
    private String title;
    private String description;
    private int unlockLevel;
    private long displayOrder;
    private boolean published;
}
