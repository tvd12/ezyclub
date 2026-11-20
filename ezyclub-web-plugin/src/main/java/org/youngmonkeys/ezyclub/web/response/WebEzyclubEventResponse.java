package org.youngmonkeys.ezyclub.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebEzyclubEventResponse {
    private long id;
    private String title;
    private String location;
    private String description;
    private String meetingUrl;
    private long startAt;
    private long finishAt;
    private long attendeeCount;
    private boolean attending;
    private boolean editable;
}
