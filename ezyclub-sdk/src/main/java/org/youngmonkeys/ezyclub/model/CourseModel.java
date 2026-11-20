package org.youngmonkeys.ezyclub.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseModel {
    private long id;
    private long communityId;
    private String title;
    private String description;
    private long featuredImageId;
    private int unlockLevel;
    private long displayOrder;
    private String status;
    private long createdAt;
}
