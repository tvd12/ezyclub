package org.youngmonkeys.ezyclub.web.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WebEzyclubPageResponse {
    private List<?> items;
    private int page;
    private boolean hasMore;
}
