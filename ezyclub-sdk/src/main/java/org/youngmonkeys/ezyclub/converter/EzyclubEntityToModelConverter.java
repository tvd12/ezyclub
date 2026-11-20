package org.youngmonkeys.ezyclub.converter;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyarticle.sdk.entity.Comment;
import org.youngmonkeys.ezyarticle.sdk.entity.Post;
import org.youngmonkeys.ezyclub.constant.MemberRole;
import org.youngmonkeys.ezyclub.model.CommentModel;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.CourseModel;
import org.youngmonkeys.ezyclub.model.EventModel;
import org.youngmonkeys.ezyclub.model.FeedPostModel;
import org.youngmonkeys.ezyclub.model.LessonModel;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.util.EzyclubLevels;
import org.youngmonkeys.ezycrm.entity.EzyCRMEvent;
import org.youngmonkeys.ezycrm.entity.UserGroup;
import org.youngmonkeys.ezycrm.entity.UserGroupMember;
import org.youngmonkeys.ezyplatform.time.ClockProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_CATEGORY;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_DESCRIPTION;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_POST_CATEGORIES;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_PRIVACY;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_SHORT_DESCRIPTION;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_THEME_COLOR;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.PRIORITY_PINNED;

@EzySingleton
@AllArgsConstructor
public class EzyclubEntityToModelConverter {

    private final ClockProxy clock;

    public CommunityModel toModel(
        UserGroup entity,
        Map<String, String> metaValues,
        Map<String, String> metaTextValues
    ) {
        if (entity == null) {
            return null;
        }
        return CommunityModel.builder()
            .id(entity.getId())
            .slug(entity.getGroupCode())
            .name(entity.getGroupName())
            .shortDescription(metaValues.getOrDefault(META_KEY_SHORT_DESCRIPTION, ""))
            .description(metaTextValues.getOrDefault(META_KEY_DESCRIPTION, ""))
            .category(metaValues.getOrDefault(META_KEY_CATEGORY, ""))
            .privacy(metaValues.getOrDefault(META_KEY_PRIVACY, ""))
            .postCategories(splitCategories(metaValues.get(META_KEY_POST_CATEGORIES)))
            .themeColor(metaValues.getOrDefault(META_KEY_THEME_COLOR, ""))
            .bannerImageId(entity.getBannerId())
            .logoImageId(entity.getLogoId())
            .status(entity.getStatus())
            .memberCount(entity.getPriority())
            .createdAt(toTimestamp(entity.getCreatedAt()))
            .build();
    }

    public MemberModel toModel(UserGroupMember entity) {
        if (entity == null) {
            return null;
        }
        long points = toPoints(entity.getScores());
        return MemberModel.builder()
            .id(entity.getId())
            .communityId(entity.getUserGroupId())
            .userId(entity.getUserId())
            .role(MemberRole.of(entity.getMemberType()).toString())
            .status(entity.getStatus())
            .points(points)
            .level(EzyclubLevels.levelOf(points))
            .joinedAt(toTimestamp(entity.getCreatedAt()))
            .build();
    }

    public FeedPostModel toFeedPostModel(Post entity, String category) {
        if (entity == null) {
            return null;
        }
        return FeedPostModel.builder()
            .id(entity.getId())
            .communityId(entity.getParentId())
            .authorUserId(entity.getAuthorUserId())
            .title(entity.getTitle())
            .content(entity.getContent())
            .category(category == null ? "" : category)
            .pinned(entity.getPriority() >= PRIORITY_PINNED)
            .commentCount(entity.getCommentCount())
            .status(entity.getStatus())
            .createdAt(toTimestamp(entity.getPublishedAt() != null
                ? entity.getPublishedAt()
                : entity.getCreatedAt()))
            .updatedAt(toTimestamp(entity.getUpdatedAt()))
            .build();
    }

    public CommentModel toModel(Comment entity) {
        if (entity == null) {
            return null;
        }
        return CommentModel.builder()
            .id(entity.getId())
            .postId(entity.getPostId())
            .parentId(entity.getParentId())
            .authorUserId(entity.getAuthorUserId())
            .content(entity.getContent())
            .createdAt(toTimestamp(entity.getCreatedAt()))
            .build();
    }

    public CourseModel toCourseModel(Post entity, int unlockLevel) {
        if (entity == null) {
            return null;
        }
        return CourseModel.builder()
            .id(entity.getId())
            .communityId(entity.getParentId())
            .title(entity.getTitle())
            .description(entity.getContent())
            .featuredImageId(entity.getFeaturedImageId())
            .unlockLevel(Math.max(1, unlockLevel))
            .displayOrder(entity.getPriority())
            .status(entity.getStatus())
            .createdAt(toTimestamp(entity.getCreatedAt()))
            .build();
    }

    public LessonModel toLessonModel(Post entity, String videoUrl) {
        if (entity == null) {
            return null;
        }
        return LessonModel.builder()
            .id(entity.getId())
            .courseId(entity.getParentId())
            .title(entity.getTitle())
            .content(entity.getContent())
            .videoUrl(videoUrl == null ? "" : videoUrl)
            .displayOrder(entity.getPriority())
            .status(entity.getStatus())
            .build();
    }

    public EventModel toModel(EzyCRMEvent entity) {
        if (entity == null) {
            return null;
        }
        return EventModel.builder()
            .id(entity.getId())
            .communityId(entity.getParentId())
            .creatorUserId(entity.getOrganizerId())
            .title(entity.getTitle())
            .location(entity.getSubtitle())
            .description(entity.getNotes())
            .meetingUrl(entity.getOnlineLink())
            .startAt(toTimestamp(entity.getStartAt()))
            .finishAt(toTimestamp(entity.getFinishAt()))
            .status(entity.getStatus())
            .build();
    }

    public static List<String> splitCategories(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(it -> !it.isEmpty())
            .collect(Collectors.toList());
    }

    private static long toPoints(BigDecimal scores) {
        return scores == null ? 0 : scores.longValue();
    }

    private long toTimestamp(LocalDateTime dateTime) {
        return dateTime == null ? 0 : clock.toTimestamp(dateTime);
    }
}
