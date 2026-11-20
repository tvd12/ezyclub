package org.youngmonkeys.ezyclub.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommunityModel {
    private long id;
    private String slug;
    private String name;
    private String shortDescription;
    private String description;
    private String category;
    private String privacy;
    private List<String> postCategories;
    private String themeColor;
    private long bannerImageId;
    private long logoImageId;
    private String status;
    private long memberCount;
    private long createdAt;
}
