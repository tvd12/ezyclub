package org.youngmonkeys.ezyclub.web.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebEzyclubSaveFeedPostRequest {
    private String title;
    private String content;
    private String category;
}
