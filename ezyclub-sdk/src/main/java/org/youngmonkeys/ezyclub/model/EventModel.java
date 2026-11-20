package org.youngmonkeys.ezyclub.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventModel {
    private long id;
    private long communityId;
    private long creatorUserId;
    private String title;
    private String location;
    private String description;
    private String meetingUrl;
    private long startAt;
    private long finishAt;
    private String status;
}
