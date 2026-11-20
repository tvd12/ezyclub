package org.youngmonkeys.ezyclub.web.controller.api;

import com.tvd12.ezyhttp.core.exception.HttpUnauthorizedException;
import com.tvd12.ezyhttp.core.response.ResponseEntity;
import com.tvd12.ezyhttp.server.core.annotation.Api;
import com.tvd12.ezyhttp.server.core.annotation.Authenticatable;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.annotation.DoPost;
import com.tvd12.ezyhttp.server.core.annotation.DoPut;
import com.tvd12.ezyhttp.server.core.annotation.PathVariable;
import com.tvd12.ezyhttp.server.core.annotation.RequestBody;
import com.tvd12.ezyhttp.server.core.annotation.RequestParam;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.constant.CommunityCategory;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.web.controller.service.WebEzyclubAccessControllerService;
import org.youngmonkeys.ezyclub.web.controller.service.WebEzyclubCommunityControllerService;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveCommunityRequest;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubCommunityResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubLeaderboardResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubPageResponse;
import org.youngmonkeys.ezyclub.web.validator.WebEzyclubValidator;
import org.youngmonkeys.ezyplatform.web.annotation.UserId;

import java.util.List;

@Api
@Authenticatable
@Controller("/api/v1/ezyclub")
@AllArgsConstructor
public class WebEzyclubCommunityApiController {

    private final WebEzyclubAccessControllerService accessControllerService;
    private final WebEzyclubCommunityControllerService communityControllerService;
    private final WebEzyclubValidator validator;

    @DoGet("/communities")
    public WebEzyclubPageResponse communitiesGet(
        @RequestParam("keyword") String keyword,
        @RequestParam("category") String category,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "limit", defaultValue = "12") int limit
    ) {
        validator.validatePageSize(limit);
        CommunityCategory communityCategory = CommunityCategory.of(category);
        return communityControllerService.getCommunityPage(
            keyword == null || keyword.trim().isEmpty() ? null : keyword.trim(),
            communityCategory == null ? null : communityCategory.toString(),
            Math.max(0, page),
            limit
        );
    }

    @DoPost("/communities")
    public WebEzyclubCommunityResponse communitiesPost(
        @UserId Long userId,
        @RequestBody WebEzyclubSaveCommunityRequest request
    ) {
        long currentUserId = validateLoggedIn(userId);
        validator.validate(request, true);
        CommunityModel community = communityControllerService.addCommunity(
            currentUserId,
            request
        );
        return communityControllerService.newCommunityResponse(
            community,
            accessControllerService.getActivatedMember(community, currentUserId)
        );
    }

    @DoGet("/my-communities")
    public List<WebEzyclubCommunityResponse> myCommunitiesGet(@UserId Long userId) {
        return communityControllerService.getMyCommunities(validateLoggedIn(userId));
    }

    @DoGet("/communities/{slug}")
    public WebEzyclubCommunityResponse communityGet(
        @UserId Long userId,
        @PathVariable("slug") String slug
    ) {
        CommunityModel community = accessControllerService.getCommunityBySlug(slug);
        MemberModel member = accessControllerService.getActivatedMember(community, userId);
        return communityControllerService.newCommunityResponse(community, member);
    }

    @DoPut("/communities/{communityId}")
    public ResponseEntity communityPut(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @RequestBody WebEzyclubSaveCommunityRequest request
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        accessControllerService.validateManager(community, userId);
        validator.validate(request, false);
        communityControllerService.updateCommunity(community, request);
        return ResponseEntity.noContent();
    }

    @DoPost("/communities/{communityId}/join")
    public WebEzyclubCommunityResponse communityJoinPost(
        @UserId Long userId,
        @PathVariable("communityId") long communityId
    ) {
        long currentUserId = validateLoggedIn(userId);
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = communityControllerService.joinCommunity(
            community,
            currentUserId
        );
        return communityControllerService.newCommunityResponse(
            accessControllerService.getCommunityById(communityId),
            member
        );
    }

    @DoPost("/communities/{communityId}/leave")
    public ResponseEntity communityLeavePost(
        @UserId Long userId,
        @PathVariable("communityId") long communityId
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateMember(community, userId);
        communityControllerService.leaveCommunity(community, member);
        return ResponseEntity.noContent();
    }

    @DoGet("/communities/{communityId}/members")
    public WebEzyclubPageResponse communityMembersGet(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        validator.validatePageSize(limit);
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        accessControllerService.validateCanView(community, userId);
        return communityControllerService.getMemberPage(community, Math.max(0, page), limit);
    }

    @DoGet("/communities/{communityId}/leaderboard")
    public WebEzyclubLeaderboardResponse communityLeaderboardGet(
        @UserId Long userId,
        @PathVariable("communityId") long communityId
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateCanView(community, userId);
        return communityControllerService.getLeaderboard(community, member);
    }

    private static long validateLoggedIn(Long userId) {
        if (userId == null || userId <= 0) {
            throw new HttpUnauthorizedException("login required");
        }
        return userId;
    }
}
