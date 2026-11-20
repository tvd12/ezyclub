package org.youngmonkeys.ezyclub.web.controller.api;

import com.tvd12.ezyhttp.core.response.ResponseEntity;
import com.tvd12.ezyhttp.server.core.annotation.Api;
import com.tvd12.ezyhttp.server.core.annotation.Authenticatable;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoDelete;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.annotation.DoPost;
import com.tvd12.ezyhttp.server.core.annotation.DoPut;
import com.tvd12.ezyhttp.server.core.annotation.PathVariable;
import com.tvd12.ezyhttp.server.core.annotation.RequestBody;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.CourseModel;
import org.youngmonkeys.ezyclub.model.LessonModel;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.web.controller.service.WebEzyclubAccessControllerService;
import org.youngmonkeys.ezyclub.web.controller.service.WebEzyclubClassroomControllerService;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveCourseRequest;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveLessonRequest;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubUpdateBooleanRequest;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubCourseDetailsResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubCourseResponse;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubCourseService;
import org.youngmonkeys.ezyclub.web.validator.WebEzyclubValidator;
import org.youngmonkeys.ezyplatform.web.annotation.UserId;

import java.util.List;

@Api
@Authenticatable
@Controller("/api/v1/ezyclub")
@AllArgsConstructor
public class WebEzyclubClassroomApiController {

    private final WebEzyclubAccessControllerService accessControllerService;
    private final WebEzyclubClassroomControllerService classroomControllerService;
    private final WebEzyclubCourseService courseService;
    private final WebEzyclubValidator validator;

    @DoGet("/communities/{communityId}/courses")
    public List<WebEzyclubCourseResponse> coursesGet(
        @UserId Long userId,
        @PathVariable("communityId") long communityId
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateCanView(community, userId);
        return classroomControllerService.getCourses(community, member);
    }

    @DoPost("/communities/{communityId}/courses")
    public ResponseEntity coursesPost(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @RequestBody WebEzyclubSaveCourseRequest request
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateManager(community, userId);
        validator.validate(request);
        classroomControllerService.addCourse(community, member.getUserId(), request);
        return ResponseEntity.noContent();
    }

    @DoGet("/communities/{communityId}/courses/{courseId}")
    public WebEzyclubCourseDetailsResponse courseGet(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("courseId") long courseId
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateCanView(community, userId);
        CourseModel course = classroomControllerService.getCourseInCommunity(
            community,
            member,
            courseId
        );
        return classroomControllerService.getCourseDetails(course, member);
    }

    @DoPut("/communities/{communityId}/courses/{courseId}")
    public ResponseEntity coursePut(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("courseId") long courseId,
        @RequestBody WebEzyclubSaveCourseRequest request
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateManager(community, userId);
        CourseModel course = classroomControllerService.getCourseInCommunity(
            community,
            member,
            courseId
        );
        validator.validate(request);
        classroomControllerService.updateCourse(course, request);
        return ResponseEntity.noContent();
    }

    @DoDelete("/communities/{communityId}/courses/{courseId}")
    public ResponseEntity courseDelete(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("courseId") long courseId
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateManager(community, userId);
        classroomControllerService.getCourseInCommunity(community, member, courseId);
        courseService.removeCourse(courseId);
        return ResponseEntity.noContent();
    }

    @DoPost("/communities/{communityId}/courses/{courseId}/lessons")
    public ResponseEntity lessonsPost(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("courseId") long courseId,
        @RequestBody WebEzyclubSaveLessonRequest request
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateManager(community, userId);
        CourseModel course = classroomControllerService.getCourseInCommunity(
            community,
            member,
            courseId
        );
        validator.validate(request);
        classroomControllerService.addLesson(course, member.getUserId(), request);
        return ResponseEntity.noContent();
    }

    @DoPut("/communities/{communityId}/courses/{courseId}/lessons/{lessonId}")
    public ResponseEntity lessonPut(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("courseId") long courseId,
        @PathVariable("lessonId") long lessonId,
        @RequestBody WebEzyclubSaveLessonRequest request
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateManager(community, userId);
        CourseModel course = classroomControllerService.getCourseInCommunity(
            community,
            member,
            courseId
        );
        LessonModel lesson = classroomControllerService.getLessonInCourse(course, lessonId);
        validator.validate(request);
        classroomControllerService.updateLesson(lesson, request);
        return ResponseEntity.noContent();
    }

    @DoDelete("/communities/{communityId}/courses/{courseId}/lessons/{lessonId}")
    public ResponseEntity lessonDelete(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("courseId") long courseId,
        @PathVariable("lessonId") long lessonId
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateManager(community, userId);
        CourseModel course = classroomControllerService.getCourseInCommunity(
            community,
            member,
            courseId
        );
        classroomControllerService.getLessonInCourse(course, lessonId);
        courseService.removeLesson(lessonId);
        return ResponseEntity.noContent();
    }

    @DoPut("/communities/{communityId}/courses/{courseId}/lessons/{lessonId}/completed")
    public ResponseEntity lessonCompletedPut(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("courseId") long courseId,
        @PathVariable("lessonId") long lessonId,
        @RequestBody WebEzyclubUpdateBooleanRequest request
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateMember(community, userId);
        CourseModel course = classroomControllerService.getCourseInCommunity(
            community,
            member,
            courseId
        );
        classroomControllerService.validateUnlocked(course, member);
        LessonModel lesson = classroomControllerService.getLessonInCourse(course, lessonId);
        classroomControllerService.updateLessonCompleted(member, lesson, request.isValue());
        return ResponseEntity.noContent();
    }
}
