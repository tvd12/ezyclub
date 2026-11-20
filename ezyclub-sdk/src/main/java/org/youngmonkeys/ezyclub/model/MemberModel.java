package org.youngmonkeys.ezyclub.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberModel {
    private long id;
    private long communityId;
    private long userId;
    private String role;
    private String status;
    private long points;
    private int level;
    private long joinedAt;
}
