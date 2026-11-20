package org.youngmonkeys.ezyclub.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeedPostModel {
    private long id;
    private long communityId;
    private long authorUserId;
    private String title;
    private String content;
    private String category;
    private boolean pinned;
    private long commentCount;
    private String status;
    private long createdAt;
    private long updatedAt;
}
