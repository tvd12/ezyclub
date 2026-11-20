package org.youngmonkeys.ezyclub.web.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WebEzyclubSaveCommunityRequest {
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
}
