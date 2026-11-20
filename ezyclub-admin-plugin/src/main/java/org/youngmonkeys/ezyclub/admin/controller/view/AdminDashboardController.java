package org.youngmonkeys.ezyclub.admin.controller.view;

import com.tvd12.ezyfox.annotation.EzyFeature;
import com.tvd12.ezyhttp.server.core.annotation.Authenticated;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.view.View;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.admin.service.AdminEzyclubCommunityService;
import org.youngmonkeys.ezyclub.admin.service.AdminEzyclubSettingService;

@Controller
@Authenticated
@EzyFeature("ezyclub")
@AllArgsConstructor
public class AdminDashboardController {

    private final AdminEzyclubSettingService settingService;
    private final AdminEzyclubCommunityService communityService;

    @DoGet("/dashboard")
    public View dashboardGet() {
        return View.builder()
            .template("ezyclub/dashboard")
            .addVariable("settings", settingService.getSettings())
            .addVariable("webUrl", settingService.getWebUrl())
            .addVariable("communityCount", communityService.countCommunities())
            .addVariable("activeCommunityCount", communityService.countActivatedCommunities())
            .build();
    }

    @DoGet("/communities")
    public View communitiesGet() {
        return View.builder()
            .template("ezyclub/communities")
            .addVariable("webUrl", settingService.getWebUrl())
            .build();
    }
}
