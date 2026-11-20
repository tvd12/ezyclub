package org.youngmonkeys.ezyclub.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SaveFeedPostModel {
    private String title;
    private String content;
    private String category;
}
