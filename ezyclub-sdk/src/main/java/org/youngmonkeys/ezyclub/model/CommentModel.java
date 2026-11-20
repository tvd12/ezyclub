package org.youngmonkeys.ezyclub.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentModel {
    private long id;
    private long postId;
    private long parentId;
    private long authorUserId;
    private String content;
    private long createdAt;
}
