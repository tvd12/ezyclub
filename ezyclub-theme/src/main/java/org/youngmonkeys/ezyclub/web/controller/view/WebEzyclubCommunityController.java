package org.youngmonkeys.ezyclub.web.controller.view;

import com.tvd12.ezyhttp.server.core.annotation.Authenticatable;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.annotation.PathVariable;
import com.tvd12.ezyhttp.server.core.view.Redirect;
import com.tvd12.ezyhttp.server.core.view.View;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.service.EzyclubMemberService;
import org.youngmonkeys.ezyclub.web.controller.service.WebEzyclubAccessControllerService;
import org.youngmonkeys.ezyclub.web.view.WebEzyclubPageViewFactory;
import org.youngmonkeys.ezyplatform.web.annotation.UserId;

@Authenticatable
@Controller("/c")
@AllArgsConstructor
public class WebEzyclubCommunityController {

    private final WebEzyclubAccessControllerService accessControllerService;
    private final WebEzyclubPageViewFactory pageViewFactory;

    @DoGet("/{slug}")
    public View communityGet(
        @UserId Long userId,
        @PathVariable("slug") String slug
    ) {
        return newCommunityView(userId, slug, "community", "community/feed", "community")
            .addVariable("openPostId", 0L)
            .build();
    }

    @DoGet("/{slug}/posts/{postId}")
    public View communityPostGet(
        @UserId Long userId,
        @PathVariable("slug") String slug,
        @PathVariable("postId") long postId
    ) {
        return newCommunityView(userId, slug, "community", "community/feed", "community")
            .addVariable("openPostId", postId)
            .build();
    }

    @DoGet("/{slug}/classroom")
    public View classroomGet(
        @UserId Long userId,
        @PathVariable("slug") String slug
    ) {
        return newCommunityView(
            userId,
            slug,
            "classroom",
            "community/classroom",
            "classroom"
        ).build();
    }

    @DoGet("/{slug}/classroom/{courseId}")
    public View courseGet(
        @UserId Long userId,
        @PathVariable("slug") String slug,
        @PathVariable("courseId") long courseId
    ) {
        return newCommunityView(userId, slug, "classroom", "community/course", "classroom")
            .addVariable("courseId", courseId)
            .build();
    }

    @DoGet("/{slug}/calendar")
    public View calendarGet(
        @UserId Long userId,
        @PathVariable("slug") String slug
    ) {
        return newCommunityView(userId, slug, "calendar", "community/calendar", "calendar")
            .build();
    }

    @DoGet("/{slug}/members")
    public View membersGet(
        @UserId Long userId,
        @PathVariable("slug") String slug
    ) {
        return newCommunityView(userId, slug, "members", "community/members", "members")
            .build();
    }

    @DoGet("/{slug}/leaderboards")
    public View leaderboardsGet(
        @UserId Long userId,
        @PathVariable("slug") String slug
    ) {
        return newCommunityView(
            userId,
            slug,
            "leaderboards",
            "community/leaderboards",
            "leaderboards"
        ).build();
    }

    @DoGet("/{slug}/about")
    public View aboutGet(
        @UserId Long userId,
        @PathVariable("slug") String slug
    ) {
        CommunityModel community = accessControllerService.getCommunityBySlug(slug);
        MemberModel member = accessControllerService.getActivatedMember(community, userId);
        return pageViewFactory
            .newCommunityViewBuilder(
                "about",
                "community/about",
                "about",
                community,
                member,
                accessControllerService.canView(community, member)
            )
            .build();
    }

    @DoGet("/{slug}/settings")
    public Object settingsGet(
        @UserId Long userId,
        @PathVariable("slug") String slug
    ) {
        CommunityModel community = accessControllerService.getCommunityBySlug(slug);
        MemberModel member = accessControllerService.getActivatedMember(community, userId);
        if (!EzyclubMemberService.isManager(member)) {
            return Redirect.to("/c/" + community.getSlug() + "/about");
        }
        return pageViewFactory
            .newCommunityViewBuilder(
                "settings",
                "community/settings",
                "community_settings",
                community,
                member,
                true
            )
            .build();
    }

    private View.Builder newCommunityView(
        Long userId,
        String slug,
        String tab,
        String template,
        String pageTitle
    ) {
        CommunityModel community = accessControllerService.getCommunityBySlug(slug);
        MemberModel member = accessControllerService.getActivatedMember(community, userId);
        boolean canView = accessControllerService.canView(community, member);
        return pageViewFactory.newCommunityViewBuilder(
            canView ? tab : "about",
            canView ? template : "community/about",
            canView ? pageTitle : "about",
            community,
            member,
            canView
        );
    }
}
