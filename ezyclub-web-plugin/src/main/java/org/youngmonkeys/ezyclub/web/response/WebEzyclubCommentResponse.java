package org.youngmonkeys.ezyclub.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebEzyclubCommentResponse {
    private long id;
    private long postId;
    private long parentId;
    private String content;
    private long likeCount;
    private boolean liked;
    private boolean editable;
    private long createdAt;
    private WebEzyclubUserResponse author;
}
