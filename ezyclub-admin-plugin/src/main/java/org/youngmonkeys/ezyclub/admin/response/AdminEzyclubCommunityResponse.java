package org.youngmonkeys.ezyclub.admin.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminEzyclubCommunityResponse {
    private long id;
    private String slug;
    private String name;
    private String category;
    private String privacy;
    private String status;
    private long memberCount;
    private long createdAt;
    private String url;
}
