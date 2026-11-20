package org.youngmonkeys.ezyclub.web.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebEzyclubAddCommentRequest {
    private String content;
    private long parentId;
}
