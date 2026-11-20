package org.youngmonkeys.ezyclub.web.controller.service;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyarticle.sdk.entity.PostStatus;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.CourseModel;
import org.youngmonkeys.ezyclub.model.LessonModel;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.model.SaveCourseModel;
import org.youngmonkeys.ezyclub.model.SaveLessonModel;
import org.youngmonkeys.ezyclub.service.EzyclubMemberService;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveCourseRequest;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveLessonRequest;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubCourseDetailsResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubCourseResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubLessonResponse;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubCourseService;
import org.youngmonkeys.ezyplatform.exception.ForbiddenActionException;
import org.youngmonkeys.ezyplatform.exception.ResourceNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EzySingleton
@AllArgsConstructor
public class WebEzyclubClassroomControllerService {

    private final WebEzyclubCourseService courseService;

    public long addCourse(
        CommunityModel community,
        long userId,
        WebEzyclubSaveCourseRequest request
    ) {
        return courseService.addCourse(community.getId(), userId, toSaveModel(request));
    }

    public void updateCourse(CourseModel course, WebEzyclubSaveCourseRequest request) {
        courseService.updateCourse(course.getId(), toSaveModel(request));
    }

    public long addLesson(
        CourseModel course,
        long userId,
        WebEzyclubSaveLessonRequest request
    ) {
        return courseService.addLesson(course.getId(), userId, toSaveModel(request));
    }

    public void updateLesson(LessonModel lesson, WebEzyclubSaveLessonRequest request) {
        courseService.updateLesson(lesson.getId(), toSaveModel(request));
    }

    public CourseModel getCourseInCommunity(
        CommunityModel community,
        MemberModel member,
        long courseId
    ) {
        CourseModel course = courseService.getVisibleCourseByIdOrThrow(courseId);
        if (course.getCommunityId() != community.getId()) {
            throw new ResourceNotFoundException("course");
        }
        if (!isPublished(course.getStatus()) && !EzyclubMemberService.isManager(member)) {
            throw new ResourceNotFoundException("course");
        }
        return course;
    }

    public LessonModel getLessonInCourse(CourseModel course, long lessonId) {
        LessonModel lesson = courseService.getLessonById(lessonId);
        if (lesson == null
            || lesson.getCourseId() != course.getId()
            || PostStatus.TRASH.toString().equals(lesson.getStatus())
        ) {
            throw new ResourceNotFoundException("lesson");
        }
        return lesson;
    }

    public void validateUnlocked(CourseModel course, MemberModel member) {
        if (isLocked(course, member)) {
            throw new ForbiddenActionException("course_locked");
        }
    }

    public void updateLessonCompleted(MemberModel member, LessonModel lesson, boolean value) {
        courseService.updateLessonCompleted(member.getUserId(), lesson.getId(), value);
    }

    public List<WebEzyclubCourseResponse> getCourses(
        CommunityModel community,
        MemberModel member
    ) {
        boolean manager = EzyclubMemberService.isManager(member);
        Set<Long> completedLessonIds = member != null
            ? courseService.getCompletedLessonIds(member.getUserId())
            : Collections.emptySet();
        return courseService
            .getCourses(community.getId(), manager)
            .stream()
            .map(course -> {
                List<LessonModel> lessons = courseService.getLessons(course.getId(), false);
                return newCourseResponse(course, member, lessons, completedLessonIds);
            })
            .collect(Collectors.toList());
    }

    public WebEzyclubCourseDetailsResponse getCourseDetails(
        CourseModel course,
        MemberModel member
    ) {
        boolean manager = EzyclubMemberService.isManager(member);
        Set<Long> completedLessonIds = member != null
            ? courseService.getCompletedLessonIds(member.getUserId())
            : Collections.emptySet();
        List<LessonModel> lessons = courseService.getLessons(course.getId(), manager);
        List<LessonModel> publishedLessons = lessons
            .stream()
            .filter(it -> isPublished(it.getStatus()))
            .collect(Collectors.toList());
        boolean locked = isLocked(course, member);
        return WebEzyclubCourseDetailsResponse.builder()
            .course(newCourseResponse(course, member, publishedLessons, completedLessonIds))
            .lessons(
                locked
                    ? Collections.emptyList()
                    : lessons
                        .stream()
                        .map(it -> WebEzyclubLessonResponse.builder()
                            .id(it.getId())
                            .courseId(it.getCourseId())
                            .title(it.getTitle())
                            .content(it.getContent())
                            .videoUrl(it.getVideoUrl())
                            .displayOrder(it.getDisplayOrder())
                            .published(isPublished(it.getStatus()))
                            .completed(completedLessonIds.contains(it.getId()))
                            .build()
                        )
                        .collect(Collectors.toList())
            )
            .build();
    }

    private WebEzyclubCourseResponse newCourseResponse(
        CourseModel course,
        MemberModel member,
        List<LessonModel> publishedLessons,
        Set<Long> completedLessonIds
    ) {
        long lessonCount = publishedLessons
            .stream()
            .filter(it -> isPublished(it.getStatus()))
            .count();
        long completedCount = publishedLessons
            .stream()
            .filter(it -> isPublished(it.getStatus()))
            .filter(it -> completedLessonIds.contains(it.getId()))
            .count();
        return WebEzyclubCourseResponse.builder()
            .id(course.getId())
            .title(course.getTitle())
            .description(course.getDescription())
            .unlockLevel(course.getUnlockLevel())
            .displayOrder(course.getDisplayOrder())
            .published(isPublished(course.getStatus()))
            .locked(isLocked(course, member))
            .lessonCount(lessonCount)
            .completedLessonCount(completedCount)
            .progress(lessonCount == 0 ? 0 : (int) (completedCount * 100 / lessonCount))
            .build();
    }

    private static boolean isLocked(CourseModel course, MemberModel member) {
        if (EzyclubMemberService.isManager(member)) {
            return false;
        }
        int level = member != null ? member.getLevel() : 0;
        return member == null || level < course.getUnlockLevel();
    }

    private static boolean isPublished(String status) {
        return PostStatus.PUBLISHED.toString().equals(status);
    }

    private static String toStatus(boolean published) {
        return published ? PostStatus.PUBLISHED.toString() : PostStatus.DRAFT.toString();
    }

    private static SaveCourseModel toSaveModel(WebEzyclubSaveCourseRequest request) {
        return SaveCourseModel.builder()
            .title(request.getTitle().trim())
            .description(request.getDescription() == null ? "" : request.getDescription())
            .unlockLevel(request.getUnlockLevel())
            .displayOrder(request.getDisplayOrder())
            .status(toStatus(request.isPublished()))
            .build();
    }

    private static SaveLessonModel toSaveModel(WebEzyclubSaveLessonRequest request) {
        return SaveLessonModel.builder()
            .title(request.getTitle().trim())
            .content(request.getContent() == null ? "" : request.getContent())
            .videoUrl(request.getVideoUrl() == null ? "" : request.getVideoUrl().trim())
            .displayOrder(request.getDisplayOrder())
            .status(toStatus(request.isPublished()))
            .build();
    }
}
