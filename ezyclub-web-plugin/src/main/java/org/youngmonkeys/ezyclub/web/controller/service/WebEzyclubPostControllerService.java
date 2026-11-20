package org.youngmonkeys.ezyclub.web.controller.service;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.model.CommentModel;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.EzyclubPageModel;
import org.youngmonkeys.ezyclub.model.FeedPostModel;
import org.youngmonkeys.ezyclub.model.LikeResultModel;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.model.SaveFeedPostModel;
import org.youngmonkeys.ezyclub.service.EzyclubMemberService;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveFeedPostRequest;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubCommentResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubFeedPostResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubLikeResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubPageResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubUserResponse;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubFeedPostService;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubLikeService;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubMemberService;
import org.youngmonkeys.ezyplatform.exception.ForbiddenActionException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.ITEM_TYPE_COMMENT;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.ITEM_TYPE_POST;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.POINTS_PER_LIKE;

@EzySingleton
@AllArgsConstructor
public class WebEzyclubPostControllerService {

    private final WebEzyclubFeedPostService feedPostService;
    private final WebEzyclubLikeService likeService;
    private final WebEzyclubMemberService memberService;
    private final WebEzyclubCommunityControllerService communityControllerService;

    private static final int MAX_COMMENTS = 200;

    public long addPost(
        CommunityModel community,
        long userId,
        WebEzyclubSaveFeedPostRequest request
    ) {
        return feedPostService.addPost(community.getId(), userId, toSaveModel(request));
    }

    public void updatePost(FeedPostModel post, WebEzyclubSaveFeedPostRequest request) {
        feedPostService.updatePost(post.getId(), toSaveModel(request));
    }

    public FeedPostModel getPostInCommunity(CommunityModel community, long postId) {
        FeedPostModel post = feedPostService.getPublishedPostByIdOrThrow(postId);
        if (post.getCommunityId() != community.getId()) {
            throw new ForbiddenActionException("post_not_in_community");
        }
        return post;
    }

    public void validateCanEdit(FeedPostModel post, MemberModel member) {
        if (post.getAuthorUserId() != member.getUserId()
            && !EzyclubMemberService.isManager(member)
        ) {
            throw new ForbiddenActionException("edit_post");
        }
    }

    public CommentModel getCommentOfPost(FeedPostModel post, long commentId) {
        CommentModel comment = feedPostService.getCommentById(commentId);
        if (comment == null || comment.getPostId() != post.getId()) {
            throw new ForbiddenActionException("comment_not_in_post");
        }
        return comment;
    }

    public void validateCanRemove(CommentModel comment, MemberModel member) {
        if (comment.getAuthorUserId() != member.getUserId()
            && !EzyclubMemberService.isManager(member)
        ) {
            throw new ForbiddenActionException("remove_comment");
        }
    }

    public WebEzyclubLikeResponse toggleLike(
        CommunityModel community,
        String itemType,
        long itemId,
        long authorUserId,
        long userId
    ) {
        LikeResultModel result = likeService.toggleLike(itemType, itemId, userId);
        if (authorUserId != userId) {
            memberService.increasePoints(
                community.getId(),
                authorUserId,
                result.isLiked() ? POINTS_PER_LIKE : -POINTS_PER_LIKE
            );
        }
        return WebEzyclubLikeResponse.builder()
            .liked(result.isLiked())
            .likeCount(result.getLikeCount())
            .build();
    }

    public WebEzyclubPageResponse getPostPage(
        CommunityModel community,
        MemberModel member,
        String category,
        int page,
        int limit
    ) {
        EzyclubPageModel<FeedPostModel> pageModel = feedPostService
            .getPublishedPostPage(community.getId(), category, page, limit);
        return WebEzyclubPageResponse.builder()
            .items(toPostResponses(community, member, pageModel.getItems()))
            .page(page)
            .hasMore(pageModel.isHasMore())
            .build();
    }

    public WebEzyclubFeedPostResponse getPostResponse(
        CommunityModel community,
        MemberModel member,
        FeedPostModel post
    ) {
        return toPostResponses(
            community,
            member,
            Collections.singletonList(post)
        ).get(0);
    }

    public List<WebEzyclubCommentResponse> getComments(
        CommunityModel community,
        MemberModel member,
        FeedPostModel post
    ) {
        List<CommentModel> comments = feedPostService.getAcceptedComments(
            post.getId(),
            MAX_COMMENTS
        );
        List<Long> commentIds = comments
            .stream()
            .map(CommentModel::getId)
            .collect(Collectors.toList());
        Map<Long, Long> likeCountById = likeService.getLikeCountMap(
            ITEM_TYPE_COMMENT,
            commentIds
        );
        long userId = member != null ? member.getUserId() : 0;
        Set<Long> likedIds = likeService.getLikedItemIds(
            ITEM_TYPE_COMMENT,
            commentIds,
            userId
        );
        Map<Long, WebEzyclubUserResponse> userById = communityControllerService
            .getUserResponseMap(
                community.getId(),
                comments
                    .stream()
                    .map(CommentModel::getAuthorUserId)
                    .distinct()
                    .collect(Collectors.toList())
            );
        boolean manager = EzyclubMemberService.isManager(member);
        return comments
            .stream()
            .map(it -> WebEzyclubCommentResponse.builder()
                .id(it.getId())
                .postId(it.getPostId())
                .parentId(it.getParentId())
                .content(it.getContent())
                .likeCount(likeCountById.getOrDefault(it.getId(), 0L))
                .liked(likedIds.contains(it.getId()))
                .editable(manager || (userId > 0 && it.getAuthorUserId() == userId))
                .createdAt(it.getCreatedAt())
                .author(userById.get(it.getAuthorUserId()))
                .build()
            )
            .collect(Collectors.toList());
    }

    private List<WebEzyclubFeedPostResponse> toPostResponses(
        CommunityModel community,
        MemberModel member,
        List<FeedPostModel> posts
    ) {
        List<Long> postIds = posts
            .stream()
            .map(FeedPostModel::getId)
            .collect(Collectors.toList());
        Map<Long, Long> likeCountById = likeService.getLikeCountMap(ITEM_TYPE_POST, postIds);
        long userId = member != null ? member.getUserId() : 0;
        Set<Long> likedIds = likeService.getLikedItemIds(ITEM_TYPE_POST, postIds, userId);
        Map<Long, WebEzyclubUserResponse> userById = communityControllerService
            .getUserResponseMap(
                community.getId(),
                posts
                    .stream()
                    .map(FeedPostModel::getAuthorUserId)
                    .distinct()
                    .collect(Collectors.toList())
            );
        boolean manager = EzyclubMemberService.isManager(member);
        return posts
            .stream()
            .map(it -> WebEzyclubFeedPostResponse.builder()
                .id(it.getId())
                .communityId(it.getCommunityId())
                .title(it.getTitle())
                .content(it.getContent())
                .category(it.getCategory())
                .pinned(it.isPinned())
                .commentCount(it.getCommentCount())
                .likeCount(likeCountById.getOrDefault(it.getId(), 0L))
                .liked(likedIds.contains(it.getId()))
                .editable(manager || (userId > 0 && it.getAuthorUserId() == userId))
                .createdAt(it.getCreatedAt())
                .updatedAt(it.getUpdatedAt())
                .author(userById.get(it.getAuthorUserId()))
                .build()
            )
            .collect(Collectors.toList());
    }

    private static SaveFeedPostModel toSaveModel(WebEzyclubSaveFeedPostRequest request) {
        return SaveFeedPostModel.builder()
            .title(request.getTitle().trim())
            .content(request.getContent().trim())
            .category(request.getCategory() == null ? "" : request.getCategory().trim())
            .build();
    }
}
