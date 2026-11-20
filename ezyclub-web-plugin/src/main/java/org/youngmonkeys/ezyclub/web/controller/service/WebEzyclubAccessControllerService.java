package org.youngmonkeys.ezyclub.web.controller.service;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import com.tvd12.ezyhttp.core.exception.HttpUnauthorizedException;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.constant.CommunityPrivacy;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.service.EzyclubMemberService;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubCommunityService;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubMemberService;
import org.youngmonkeys.ezyplatform.exception.ForbiddenActionException;

@EzySingleton
@AllArgsConstructor
public class WebEzyclubAccessControllerService {

    private final WebEzyclubCommunityService communityService;
    private final WebEzyclubMemberService memberService;

    public CommunityModel getCommunityBySlug(String slug) {
        return communityService.getActivatedCommunityBySlugOrThrow(slug);
    }

    public CommunityModel getCommunityById(long communityId) {
        return communityService.getActivatedCommunityByIdOrThrow(communityId);
    }

    public MemberModel getActivatedMember(CommunityModel community, Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return memberService.getActivatedMember(community.getId(), userId);
    }

    public boolean canView(CommunityModel community, MemberModel member) {
        return !CommunityPrivacy.PRIVATE.toString().equals(community.getPrivacy())
            || member != null;
    }

    public MemberModel validateCanView(CommunityModel community, Long userId) {
        MemberModel member = getActivatedMember(community, userId);
        if (!canView(community, member)) {
            throw new ForbiddenActionException("view_private_community");
        }
        return member;
    }

    public MemberModel validateMember(CommunityModel community, Long userId) {
        if (userId == null || userId <= 0) {
            throw new HttpUnauthorizedException("login required");
        }
        MemberModel member = getActivatedMember(community, userId);
        if (member == null) {
            throw new ForbiddenActionException("join_community_required");
        }
        return member;
    }

    public MemberModel validateManager(CommunityModel community, Long userId) {
        MemberModel member = validateMember(community, userId);
        if (!EzyclubMemberService.isManager(member)) {
            throw new ForbiddenActionException("manage_community");
        }
        return member;
    }
}
