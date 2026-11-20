package org.youngmonkeys.ezyclub.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebEzyclubUserResponse {
    private String uuid;
    private String username;
    private String displayName;
    private String avatarUrl;
    private int level;
    private long points;
    private String role;
}
