package org.youngmonkeys.ezyclub.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SaveCourseModel {
    private String title;
    private String description;
    private int unlockLevel;
    private long displayOrder;
    private String status;
}
