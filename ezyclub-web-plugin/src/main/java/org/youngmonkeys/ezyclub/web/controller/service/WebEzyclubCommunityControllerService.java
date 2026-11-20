package org.youngmonkeys.ezyclub.web.controller.service;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.constant.MemberRole;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.EzyclubPageModel;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.model.SaveCommunityModel;
import org.youngmonkeys.ezyclub.service.EzyclubMemberService;
import org.youngmonkeys.ezyclub.util.EzyclubLevels;
import org.youngmonkeys.ezyclub.web.converter.WebEzyclubUserResponseFactory;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveCommunityRequest;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubCommunityResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubLeaderboardResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubPageResponse;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubUserResponse;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubCommunityService;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubMemberService;
import org.youngmonkeys.ezycrm.entity.UserGroupStatus;
import org.youngmonkeys.ezyplatform.exception.ForbiddenActionException;
import org.youngmonkeys.ezyplatform.model.MediaNameModel;
import org.youngmonkeys.ezyplatform.web.service.WebMediaService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EzySingleton
@AllArgsConstructor
public class WebEzyclubCommunityControllerService {

    private final WebEzyclubCommunityService communityService;
    private final WebEzyclubMemberService memberService;
    private final WebEzyclubUserResponseFactory userResponseFactory;
    private final WebMediaService mediaService;

    private static final int LEADERBOARD_SIZE = 10;
    private static final int MAX_MY_COMMUNITIES = 50;

    public CommunityModel addCommunity(long userId, WebEzyclubSaveCommunityRequest request) {
        long communityId = communityService.addCommunity(
            userId,
            toSaveModel(request, request.getSlug().trim())
        );
        return communityService.getCommunityById(communityId);
    }

    public void updateCommunity(
        CommunityModel community,
        WebEzyclubSaveCommunityRequest request
    ) {
        communityService.updateCommunity(
            community.getId(),
            toSaveModel(request, community.getSlug())
        );
    }

    public MemberModel joinCommunity(CommunityModel community, long userId) {
        MemberModel member = memberService.joinCommunity(community.getId(), userId);
        communityService.refreshMemberCount(community.getId());
        return member;
    }

    public void leaveCommunity(CommunityModel community, MemberModel member) {
        if (MemberRole.OWNER.toString().equals(member.getRole())) {
            throw new ForbiddenActionException("owner_leave_community");
        }
        memberService.leaveCommunity(community.getId(), member.getUserId());
        communityService.refreshMemberCount(community.getId());
    }

    public WebEzyclubCommunityResponse newCommunityResponse(
        CommunityModel community,
        MemberModel member
    ) {
        return newCommunityResponse(
            community,
            member,
            getMediaNameMap(Collections.singletonList(community))
        );
    }

    public List<WebEzyclubCommunityResponse> newCommunityResponses(
        List<CommunityModel> communities,
        Map<Long, MemberModel> memberByCommunityId
    ) {
        Map<Long, MediaNameModel> mediaById = getMediaNameMap(communities);
        return communities
            .stream()
            .map(it ->
                newCommunityResponse(
                    it,
                    memberByCommunityId.get(it.getId()),
                    mediaById
                )
            )
            .collect(Collectors.toList());
    }

    private WebEzyclubCommunityResponse newCommunityResponse(
        CommunityModel community,
        MemberModel member,
        Map<Long, MediaNameModel> mediaById
    ) {
        return WebEzyclubCommunityResponse.builder()
            .id(community.getId())
            .slug(community.getSlug())
            .name(community.getName())
            .shortDescription(community.getShortDescription())
            .description(community.getDescription())
            .category(community.getCategory())
            .privacy(community.getPrivacy())
            .postCategories(community.getPostCategories())
            .themeColor(community.getThemeColor())
            .bannerImageId(community.getBannerImageId())
            .bannerUrl(toMediaUrl(mediaById.get(community.getBannerImageId())))
            .logoImageId(community.getLogoImageId())
            .logoUrl(toMediaUrl(mediaById.get(community.getLogoImageId())))
            .memberCount(community.getMemberCount())
            .createdAt(community.getCreatedAt())
            .joined(member != null)
            .manager(EzyclubMemberService.isManager(member))
            .build();
    }

    private Map<Long, MediaNameModel> getMediaNameMap(
        Collection<CommunityModel> communities
    ) {
        Set<Long> mediaIds = communities
            .stream()
            .flatMap(it -> Stream.of(it.getBannerImageId(), it.getLogoImageId()))
            .filter(it -> it > 0)
            .collect(Collectors.toSet());
        return mediaIds.isEmpty()
            ? Collections.emptyMap()
            : mediaService.getMediaNameMapByIds(mediaIds);
    }

    private static String toMediaUrl(MediaNameModel media) {
        return media != null ? "/api/v1/media/" + media.getName() : "";
    }

    public WebEzyclubPageResponse getCommunityPage(
        String keyword,
        String category,
        int page,
        int limit
    ) {
        EzyclubPageModel<CommunityModel> pageModel = communityService
            .getActivatedCommunityPage(keyword, category, page, limit);
        return WebEzyclubPageResponse.builder()
            .items(
                newCommunityResponses(
                    pageModel.getItems(),
                    Collections.emptyMap()
                )
            )
            .page(page)
            .hasMore(pageModel.isHasMore())
            .build();
    }

    public List<WebEzyclubCommunityResponse> getMyCommunities(long userId) {
        List<MemberModel> memberships = memberService.getActivatedMembershipsOfUser(
            userId,
            MAX_MY_COMMUNITIES
        );
        Map<Long, MemberModel> memberByCommunityId = memberships
            .stream()
            .collect(Collectors.toMap(MemberModel::getCommunityId, Function.identity()));
        List<CommunityModel> communities = communityService
            .getCommunitiesByIds(memberByCommunityId.keySet())
            .stream()
            .filter(it -> UserGroupStatus.ACTIVATED.toString().equals(it.getStatus()))
            .collect(Collectors.toList());
        return newCommunityResponses(communities, memberByCommunityId);
    }

    public WebEzyclubPageResponse getMemberPage(
        CommunityModel community,
        int page,
        int limit
    ) {
        EzyclubPageModel<MemberModel> pageModel = memberService.getMemberPage(
            community.getId(),
            page,
            limit
        );
        return WebEzyclubPageResponse.builder()
            .items(toUserResponses(pageModel.getItems()))
            .page(page)
            .hasMore(pageModel.isHasMore())
            .build();
    }

    public WebEzyclubLeaderboardResponse getLeaderboard(
        CommunityModel community,
        MemberModel member
    ) {
        List<MemberModel> topMembers = memberService.getTopMembers(
            community.getId(),
            LEADERBOARD_SIZE
        );
        List<Long> levelPoints = new ArrayList<>();
        for (int level = 1; level <= EzyclubLevels.maxLevel(); ++level) {
            levelPoints.add(EzyclubLevels.pointsOfLevel(level));
        }
        WebEzyclubLeaderboardResponse.WebEzyclubLeaderboardResponseBuilder builder =
            WebEzyclubLeaderboardResponse.builder()
                .topMembers(toUserResponses(topMembers))
                .maxLevel(EzyclubLevels.maxLevel())
                .levelPoints(levelPoints);
        if (member != null) {
            builder
                .me(userResponseFactory.newUserResponse(member.getUserId(), member))
                .myRank(memberService.getMemberRank(community.getId(), member.getPoints()))
                .pointsToNextLevel(EzyclubLevels.pointsToNextLevel(member.getPoints()));
        }
        return builder.build();
    }

    public Map<Long, WebEzyclubUserResponse> getUserResponseMap(
        long communityId,
        List<Long> userIds
    ) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userResponseFactory.newUserResponseMap(
            userIds,
            memberService.getMemberMapByUserIds(communityId, userIds)
        );
    }

    private List<WebEzyclubUserResponse> toUserResponses(List<MemberModel> members) {
        Map<Long, MemberModel> memberByUserId = members
            .stream()
            .collect(Collectors.toMap(MemberModel::getUserId, Function.identity()));
        Map<Long, WebEzyclubUserResponse> userById = userResponseFactory
            .newUserResponseMap(memberByUserId.keySet(), memberByUserId);
        return members
            .stream()
            .map(it -> userById.get(it.getUserId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static SaveCommunityModel toSaveModel(
        WebEzyclubSaveCommunityRequest request,
        String slug
    ) {
        return SaveCommunityModel.builder()
            .slug(slug)
            .name(request.getName().trim())
            .shortDescription(trimToEmpty(request.getShortDescription()))
            .description(trimToEmpty(request.getDescription()))
            .category(request.getCategory())
            .privacy(request.getPrivacy())
            .themeColor(trimToEmpty(request.getThemeColor()))
            .bannerImageId(Math.max(0, request.getBannerImageId()))
            .logoImageId(Math.max(0, request.getLogoImageId()))
            .postCategories(
                request.getPostCategories() == null
                    ? Collections.emptyList()
                    : request.getPostCategories()
                        .stream()
                        .map(String::trim)
                        .distinct()
                        .collect(Collectors.toList())
            )
            .build();
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
