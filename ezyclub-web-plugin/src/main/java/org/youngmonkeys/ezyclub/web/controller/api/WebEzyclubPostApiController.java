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
import com.tvd12.ezyhttp.server.core.annotation.RequestParam;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.model.CommentModel;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.FeedPostModel;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.web.controller.service.WebEzyclubAccessControllerService;
import org.youngmonkeys.ezyclub.web.controller.service.WebEzyclubPostControllerService;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubAddCommentRequest;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveFeedPostRequest;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubUpdateBooleanRequest;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubCommentResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubFeedPostResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubLikeResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubPageResponse;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubFeedPostService;
import org.youngmonkeys.ezyclub.web.validator.WebEzyclubValidator;
import org.youngmonkeys.ezyplatform.web.annotation.UserId;

import java.util.List;

import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.ITEM_TYPE_COMMENT;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.ITEM_TYPE_POST;

@Api
@Authenticatable
@Controller("/api/v1/ezyclub")
@AllArgsConstructor
public class WebEzyclubPostApiController {

    private final WebEzyclubAccessControllerService accessControllerService;
    private final WebEzyclubPostControllerService postControllerService;
    private final WebEzyclubFeedPostService feedPostService;
    private final WebEzyclubValidator validator;

    @DoGet("/communities/{communityId}/posts")
    public WebEzyclubPageResponse postsGet(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @RequestParam("category") String category,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        validator.validatePageSize(limit);
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateCanView(community, userId);
        return postControllerService.getPostPage(
            community,
            member,
            category == null || category.trim().isEmpty() ? null : category.trim(),
            Math.max(0, page),
            limit
        );
    }

    @DoPost("/communities/{communityId}/posts")
    public WebEzyclubFeedPostResponse postsPost(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @RequestBody WebEzyclubSaveFeedPostRequest request
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateMember(community, userId);
        validator.validate(request, community.getPostCategories());
        long postId = postControllerService.addPost(community, member.getUserId(), request);
        return postControllerService.getPostResponse(
            community,
            member,
            feedPostService.getPublishedPostByIdOrThrow(postId)
        );
    }

    @DoGet("/communities/{communityId}/posts/{postId}")
    public WebEzyclubFeedPostResponse postGet(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("postId") long postId
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateCanView(community, userId);
        FeedPostModel post = postControllerService.getPostInCommunity(community, postId);
        return postControllerService.getPostResponse(community, member, post);
    }

    @DoPut("/communities/{communityId}/posts/{postId}")
    public ResponseEntity postPut(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("postId") long postId,
        @RequestBody WebEzyclubSaveFeedPostRequest request
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateMember(community, userId);
        FeedPostModel post = postControllerService.getPostInCommunity(community, postId);
        postControllerService.validateCanEdit(post, member);
        validator.validate(request, community.getPostCategories());
        postControllerService.updatePost(post, request);
        return ResponseEntity.noContent();
    }

    @DoDelete("/communities/{communityId}/posts/{postId}")
    public ResponseEntity postDelete(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("postId") long postId
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateMember(community, userId);
        FeedPostModel post = postControllerService.getPostInCommunity(community, postId);
        postControllerService.validateCanEdit(post, member);
        feedPostService.removePost(postId);
        return ResponseEntity.noContent();
    }

    @DoPut("/communities/{communityId}/posts/{postId}/pinned")
    public ResponseEntity postPinnedPut(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("postId") long postId,
        @RequestBody WebEzyclubUpdateBooleanRequest request
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        accessControllerService.validateManager(community, userId);
        postControllerService.getPostInCommunity(community, postId);
        feedPostService.updatePinned(postId, request.isValue());
        return ResponseEntity.noContent();
    }

    @DoPost("/communities/{communityId}/posts/{postId}/like")
    public WebEzyclubLikeResponse postLikePost(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("postId") long postId
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateMember(community, userId);
        FeedPostModel post = postControllerService.getPostInCommunity(community, postId);
        return postControllerService.toggleLike(
            community,
            ITEM_TYPE_POST,
            postId,
            post.getAuthorUserId(),
            member.getUserId()
        );
    }

    @DoGet("/communities/{communityId}/posts/{postId}/comments")
    public List<WebEzyclubCommentResponse> postCommentsGet(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("postId") long postId
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateCanView(community, userId);
        FeedPostModel post = postControllerService.getPostInCommunity(community, postId);
        return postControllerService.getComments(community, member, post);
    }

    @DoPost("/communities/{communityId}/posts/{postId}/comments")
    public ResponseEntity postCommentsPost(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("postId") long postId,
        @RequestBody WebEzyclubAddCommentRequest request
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateMember(community, userId);
        FeedPostModel post = postControllerService.getPostInCommunity(community, postId);
        validator.validate(request);
        long parentId = request.getParentId();
        if (parentId > 0) {
            postControllerService.getCommentOfPost(post, parentId);
        }
        feedPostService.addComment(
            post.getId(),
            member.getUserId(),
            parentId,
            request.getContent().trim()
        );
        return ResponseEntity.noContent();
    }

    @DoDelete("/communities/{communityId}/posts/{postId}/comments/{commentId}")
    public ResponseEntity postCommentDelete(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("postId") long postId,
        @PathVariable("commentId") long commentId
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateMember(community, userId);
        FeedPostModel post = postControllerService.getPostInCommunity(community, postId);
        CommentModel comment = postControllerService.getCommentOfPost(post, commentId);
        postControllerService.validateCanRemove(comment, member);
        feedPostService.removeComment(commentId);
        return ResponseEntity.noContent();
    }

    @DoPost("/communities/{communityId}/posts/{postId}/comments/{commentId}/like")
    public WebEzyclubLikeResponse postCommentLikePost(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("postId") long postId,
        @PathVariable("commentId") long commentId
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateMember(community, userId);
        FeedPostModel post = postControllerService.getPostInCommunity(community, postId);
        CommentModel comment = postControllerService.getCommentOfPost(post, commentId);
        return postControllerService.toggleLike(
            community,
            ITEM_TYPE_COMMENT,
            commentId,
            comment.getAuthorUserId(),
            member.getUserId()
        );
    }
}
