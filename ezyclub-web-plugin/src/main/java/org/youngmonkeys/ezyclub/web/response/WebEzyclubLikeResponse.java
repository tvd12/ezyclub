package org.youngmonkeys.ezyclub.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebEzyclubLikeResponse {
    private boolean liked;
    private long likeCount;
}
