package org.youngmonkeys.ezyclub.web.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebEzyclubSaveEventRequest {
    private String title;
    private String location;
    private String description;
    private String meetingUrl;
    private long startAt;
    private long finishAt;
}
