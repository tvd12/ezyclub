package org.youngmonkeys.ezyclub.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikeResultModel {
    private boolean liked;
    private long likeCount;
}
