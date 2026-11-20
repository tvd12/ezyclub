package org.youngmonkeys.ezyclub.service;

import com.tvd12.ezyfox.bean.annotation.EzyAutoBind;
import lombok.Setter;
import org.youngmonkeys.ezyarticle.sdk.entity.Post;
import org.youngmonkeys.ezyarticle.sdk.entity.PostCommentStatus;
import org.youngmonkeys.ezyarticle.sdk.entity.PostMeta;
import org.youngmonkeys.ezyarticle.sdk.entity.PostStatus;
import org.youngmonkeys.ezyarticle.sdk.repo.PostMetaRepository;
import org.youngmonkeys.ezyclub.converter.EzyclubEntityToModelConverter;
import org.youngmonkeys.ezyclub.model.CourseModel;
import org.youngmonkeys.ezyclub.model.LessonModel;
import org.youngmonkeys.ezyclub.model.SaveCourseModel;
import org.youngmonkeys.ezyclub.model.SaveLessonModel;
import org.youngmonkeys.ezyclub.repo.EzyclubPostRepository;
import org.youngmonkeys.ezyplatform.exception.ResourceNotFoundException;
import org.youngmonkeys.ezyplatform.service.DataMappingService;
import org.youngmonkeys.ezyplatform.time.ClockProxy;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.MAPPING_NAME_LESSON_COMPLETION;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_UNLOCK_LEVEL;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_VIDEO_URL;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.POST_TYPE_COURSE;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.POST_TYPE_LESSON;

@Setter
public class EzyclubCourseService {

    @EzyAutoBind
    protected EzyclubPostRepository postRepository;

    @EzyAutoBind
    protected PostMetaRepository postMetaRepository;

    @EzyAutoBind
    protected DataMappingService dataMappingService;

    @EzyAutoBind
    protected EzyclubEntityToModelConverter entityToModelConverter;

    @EzyAutoBind
    protected ClockProxy clock;

    public long addCourse(long communityId, long authorUserId, SaveCourseModel model) {
        Post entity = newPost(POST_TYPE_COURSE, communityId, authorUserId);
        mergeCourse(entity, model);
        postRepository.save(entity);
        saveNumberMeta(entity.getId(), META_KEY_UNLOCK_LEVEL, model.getUnlockLevel());
        return entity.getId();
    }

    public void updateCourse(long courseId, SaveCourseModel model) {
        Post entity = getPostEntityOrThrow(courseId, POST_TYPE_COURSE);
        mergeCourse(entity, model);
        postRepository.save(entity);
        saveNumberMeta(courseId, META_KEY_UNLOCK_LEVEL, model.getUnlockLevel());
    }

    public void removeCourse(long courseId) {
        Post entity = getPostEntityOrThrow(courseId, POST_TYPE_COURSE);
        entity.setStatus(PostStatus.TRASH.toString());
        entity.setUpdatedAt(clock.nowDateTime());
        postRepository.save(entity);
    }

    public long addLesson(long courseId, long authorUserId, SaveLessonModel model) {
        Post entity = newPost(POST_TYPE_LESSON, courseId, authorUserId);
        mergeLesson(entity, model);
        postRepository.save(entity);
        saveTextMeta(entity.getId(), META_KEY_VIDEO_URL, model.getVideoUrl());
        return entity.getId();
    }

    public void updateLesson(long lessonId, SaveLessonModel model) {
        Post entity = getPostEntityOrThrow(lessonId, POST_TYPE_LESSON);
        mergeLesson(entity, model);
        postRepository.save(entity);
        saveTextMeta(lessonId, META_KEY_VIDEO_URL, model.getVideoUrl());
    }

    public void removeLesson(long lessonId) {
        Post entity = getPostEntityOrThrow(lessonId, POST_TYPE_LESSON);
        entity.setStatus(PostStatus.TRASH.toString());
        entity.setUpdatedAt(clock.nowDateTime());
        postRepository.save(entity);
    }

    public CourseModel getCourseById(long courseId) {
        Post entity = postRepository.findById(courseId);
        if (entity == null || !POST_TYPE_COURSE.equals(entity.getPostType())) {
            return null;
        }
        return entityToModelConverter.toCourseModel(
            entity,
            getNumberMetaValue(courseId, META_KEY_UNLOCK_LEVEL)
        );
    }

    public CourseModel getVisibleCourseByIdOrThrow(long courseId) {
        CourseModel course = getCourseById(courseId);
        if (course == null || PostStatus.TRASH.toString().equals(course.getStatus())) {
            throw new ResourceNotFoundException("course");
        }
        return course;
    }

    public List<CourseModel> getCourses(long communityId, boolean includeDrafts) {
        List<Post> entities = new ArrayList<>(
            postRepository.findByParentIdAndStatusAndPostTypeOrderByPriorityAsc(
                communityId,
                PostStatus.PUBLISHED.toString(),
                POST_TYPE_COURSE
            )
        );
        if (includeDrafts) {
            entities.addAll(
                postRepository.findByParentIdAndStatusAndPostTypeOrderByPriorityAsc(
                    communityId,
                    PostStatus.DRAFT.toString(),
                    POST_TYPE_COURSE
                )
            );
        }
        Map<Long, Integer> unlockLevelByCourseId = getNumberMetaMap(
            entities.stream().map(Post::getId).collect(Collectors.toList()),
            META_KEY_UNLOCK_LEVEL
        );
        return entities
            .stream()
            .map(it -> entityToModelConverter.toCourseModel(
                it,
                unlockLevelByCourseId.getOrDefault(it.getId(), 1)
            ))
            .collect(Collectors.toList());
    }

    public LessonModel getLessonById(long lessonId) {
        Post entity = postRepository.findById(lessonId);
        if (entity == null || !POST_TYPE_LESSON.equals(entity.getPostType())) {
            return null;
        }
        return entityToModelConverter.toLessonModel(
            entity,
            getTextMetaValue(lessonId, META_KEY_VIDEO_URL)
        );
    }

    public List<LessonModel> getLessons(long courseId, boolean includeDrafts) {
        List<Post> entities = new ArrayList<>(
            postRepository.findByParentIdAndStatusAndPostTypeOrderByPriorityAsc(
                courseId,
                PostStatus.PUBLISHED.toString(),
                POST_TYPE_LESSON
            )
        );
        if (includeDrafts) {
            entities.addAll(
                postRepository.findByParentIdAndStatusAndPostTypeOrderByPriorityAsc(
                    courseId,
                    PostStatus.DRAFT.toString(),
                    POST_TYPE_LESSON
                )
            );
        }
        Map<Long, String> videoUrlByLessonId = getTextMetaMap(
            entities.stream().map(Post::getId).collect(Collectors.toList()),
            META_KEY_VIDEO_URL
        );
        return entities
            .stream()
            .map(it -> entityToModelConverter.toLessonModel(
                it,
                videoUrlByLessonId.get(it.getId())
            ))
            .collect(Collectors.toList());
    }

    public long countPublishedLessons(long courseId) {
        return postRepository.countByParentIdAndStatusAndPostType(
            courseId,
            PostStatus.PUBLISHED.toString(),
            POST_TYPE_LESSON
        );
    }

    public void updateLessonCompleted(long userId, long lessonId, boolean completed) {
        if (completed) {
            dataMappingService.saveDataMapping(
                MAPPING_NAME_LESSON_COMPLETION,
                userId,
                lessonId
            );
        } else {
            dataMappingService.removeDataMapping(
                MAPPING_NAME_LESSON_COMPLETION,
                userId,
                lessonId
            );
        }
    }

    public Set<Long> getCompletedLessonIds(long userId) {
        if (userId <= 0) {
            return Collections.emptySet();
        }
        return dataMappingService.getMappingToDataIdsByNameAndFromDataId(
            MAPPING_NAME_LESSON_COMPLETION,
            userId
        );
    }

    protected Post newPost(String postType, long parentId, long authorUserId) {
        LocalDateTime now = clock.nowDateTime();
        Post entity = new Post();
        entity.setPostType(postType);
        entity.setParentId(parentId);
        entity.setAuthorUserId(authorUserId);
        entity.setContentType("TEXT");
        entity.setCommentStatus(PostCommentStatus.CLOSE.toString());
        entity.setCreatedAt(now);
        entity.setPublishedAt(now);
        return entity;
    }

    protected void mergeCourse(Post entity, SaveCourseModel model) {
        entity.setTitle(model.getTitle());
        entity.setContent(model.getDescription());
        entity.setPriority(model.getDisplayOrder());
        entity.setStatus(model.getStatus());
        entity.setUpdatedAt(clock.nowDateTime());
    }

    protected void mergeLesson(Post entity, SaveLessonModel model) {
        entity.setTitle(model.getTitle());
        entity.setContent(model.getContent());
        entity.setPriority(model.getDisplayOrder());
        entity.setStatus(model.getStatus());
        entity.setUpdatedAt(clock.nowDateTime());
    }

    protected Post getPostEntityOrThrow(long postId, String postType) {
        Post entity = postRepository.findById(postId);
        if (entity == null || !postType.equals(entity.getPostType())) {
            throw new ResourceNotFoundException(
                POST_TYPE_COURSE.equals(postType) ? "course" : "lesson"
            );
        }
        return entity;
    }

    protected void saveNumberMeta(long postId, String metaKey, long value) {
        PostMeta entity = getOrNewPostMeta(postId, metaKey);
        entity.setMetaValue(String.valueOf(value));
        entity.setMetaNumberValue(BigInteger.valueOf(value));
        postMetaRepository.save(entity);
    }

    protected void saveTextMeta(long postId, String metaKey, String value) {
        PostMeta entity = getOrNewPostMeta(postId, metaKey);
        entity.setMetaTextValue(value == null ? "" : value);
        postMetaRepository.save(entity);
    }

    private PostMeta getOrNewPostMeta(long postId, String metaKey) {
        PostMeta entity = postMetaRepository
            .findByPostIdAndMetaKey(postId, metaKey)
            .orElseGet(PostMeta::new);
        entity.setPostId(postId);
        entity.setMetaKey(metaKey);
        if (entity.getMetaNumberValue() == null) {
            entity.setMetaNumberValue(BigInteger.ZERO);
        }
        return entity;
    }

    private int getNumberMetaValue(long postId, String metaKey) {
        return postMetaRepository
            .findByPostIdAndMetaKey(postId, metaKey)
            .map(it -> it.getMetaNumberValue() == null
                ? 1
                : it.getMetaNumberValue().intValue()
            )
            .orElse(1);
    }

    private String getTextMetaValue(long postId, String metaKey) {
        return postMetaRepository
            .findByPostIdAndMetaKey(postId, metaKey)
            .map(PostMeta::getMetaTextValue)
            .orElse("");
    }

    private Map<Long, Integer> getNumberMetaMap(List<Long> postIds, String metaKey) {
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return postMetaRepository
            .findByPostIdInAndMetaKey(postIds, metaKey)
            .stream()
            .collect(
                Collectors.toMap(
                    PostMeta::getPostId,
                    it -> it.getMetaNumberValue() == null
                        ? 1
                        : it.getMetaNumberValue().intValue(),
                    (o, n) -> n
                )
            );
    }

    private Map<Long, String> getTextMetaMap(List<Long> postIds, String metaKey) {
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return postMetaRepository
            .findByPostIdInAndMetaKey(postIds, metaKey)
            .stream()
            .collect(
                Collectors.toMap(
                    PostMeta::getPostId,
                    it -> it.getMetaTextValue() == null ? "" : it.getMetaTextValue(),
                    (o, n) -> n
                )
            );
    }
}
