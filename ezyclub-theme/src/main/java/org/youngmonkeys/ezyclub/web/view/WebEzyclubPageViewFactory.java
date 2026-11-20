package org.youngmonkeys.ezyclub.web.view;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import com.tvd12.ezyhttp.server.core.view.View;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.constant.CommunityCategory;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.service.EzyclubMemberService;
import org.youngmonkeys.ezyclub.util.EzyclubLevels;
import org.youngmonkeys.ezyclub.web.controller.service.WebEzyclubCommunityControllerService;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubFeedPostService;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubSettingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.youngmonkeys.ezyplatform.constant.CommonConstants.VIEW_VARIABLE_PAGE_TITLE;

@EzySingleton
@AllArgsConstructor
public class WebEzyclubPageViewFactory {

    private final WebEzyclubSettingService themeSettingService;
    private final WebEzyclubFeedPostService feedPostService;
    private final WebEzyclubCommunityControllerService communityControllerService;

    public static final String VIEW_VARIABLE_CURRENT_PAGE = "currentPage";
    public static final String VIEW_VARIABLE_CURRENT_TAB = "currentTab";
    public static final String VIEW_VARIABLE_THEME_STYLES = "themeStyles";
    public static final String STYLE_MAIN = "/css/main.css";
    public static final String STYLE_COMMUNITY = "/css/community.css";

    public View newHomeView() {
        return newHomeViewBuilder().build();
    }

    public View.Builder newHomeViewBuilder() {
        return newViewBuilder("home", "home", "discover_communities")
            .addVariable("communityCategories", CommunityCategory.names());
    }

    public View newCreateCommunityView() {
        return newViewBuilder("create", "community/create", "create_community")
            .addVariable("communityCategories", CommunityCategory.names())
            .build();
    }

    public View.Builder newCommunityViewBuilder(
        String tab,
        String template,
        String pageTitle,
        CommunityModel community,
        MemberModel member,
        boolean canView
    ) {
        List<Long> levelPoints = new ArrayList<>();
        for (int level = 1; level <= EzyclubLevels.maxLevel(); ++level) {
            levelPoints.add(EzyclubLevels.pointsOfLevel(level));
        }
        return newViewBuilder("community", template, pageTitle, STYLE_COMMUNITY)
            .addVariable(VIEW_VARIABLE_CURRENT_TAB, tab)
            .addVariable("community", communityControllerService.newCommunityResponse(
                community,
                member
            ))
            .addVariable("member", member)
            .addVariable("manager", EzyclubMemberService.isManager(member))
            .addVariable("canView", canView)
            .addVariable("postCount", feedPostService.countPublishedPosts(community.getId()))
            .addVariable("communityCategories", CommunityCategory.names())
            .addVariable("levelPoints", levelPoints)
            .addVariable(
                "postsPerPage",
                themeSettingService.getSettings().getPostsPerPage()
            );
    }

    private View.Builder newViewBuilder(
        String page,
        String template,
        String pageTitle,
        String... styles
    ) {
        List<String> themeStyles = new ArrayList<>();
        themeStyles.add(STYLE_MAIN);
        themeStyles.addAll(Arrays.asList(styles));
        return View.builder()
            .template(template)
            .addVariable(VIEW_VARIABLE_PAGE_TITLE, pageTitle)
            .addVariable(VIEW_VARIABLE_CURRENT_PAGE, page)
            .addVariable(VIEW_VARIABLE_THEME_STYLES, themeStyles);
    }
}
