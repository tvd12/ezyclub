package org.youngmonkeys.ezyclub.web.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WebEzyclubCommunityResponse {
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
    private String bannerUrl;
    private long logoImageId;
    private String logoUrl;
    private long memberCount;
    private long createdAt;
    private boolean joined;
    private boolean manager;
}
