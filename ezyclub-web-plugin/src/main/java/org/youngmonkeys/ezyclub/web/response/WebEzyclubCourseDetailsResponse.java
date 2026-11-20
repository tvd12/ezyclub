package org.youngmonkeys.ezyclub.web.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WebEzyclubCourseDetailsResponse {
    private WebEzyclubCourseResponse course;
    private List<WebEzyclubLessonResponse> lessons;
}
