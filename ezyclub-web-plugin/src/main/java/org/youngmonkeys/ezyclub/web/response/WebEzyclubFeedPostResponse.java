package org.youngmonkeys.ezyclub.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebEzyclubFeedPostResponse {
    private long id;
    private long communityId;
    private String title;
    private String content;
    private String category;
    private boolean pinned;
    private long commentCount;
    private long likeCount;
    private boolean liked;
    private boolean editable;
    private long createdAt;
    private long updatedAt;
    private WebEzyclubUserResponse author;
}
