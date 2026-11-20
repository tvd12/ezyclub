package org.youngmonkeys.ezyclub.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SaveEventModel {
    private String title;
    private String location;
    private String description;
    private String meetingUrl;
    private long startAt;
    private long finishAt;
}
