package org.youngmonkeys.ezyclub.web.controller.view;

import com.tvd12.ezyhttp.core.exception.HttpNotFoundException;
import com.tvd12.ezyhttp.server.core.annotation.Authenticatable;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.view.Redirect;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubSettingService;
import org.youngmonkeys.ezyclub.web.view.WebEzyclubPageViewFactory;
import org.youngmonkeys.ezyplatform.web.annotation.UserId;

@Authenticatable
@Controller
@AllArgsConstructor
public class WebEzyclubHomeController {

    private final WebEzyclubPageViewFactory pageViewFactory;
    private final WebEzyclubSettingService themeSettingService;

    @DoGet("/")
    public Object home() {
        return pageViewFactory.newHomeView();
    }

    @DoGet("/create-community")
    public Object createCommunityGet(@UserId Long userId) {
        if (!themeSettingService.getSettings().isAllowUserCreateCommunity()) {
            throw new HttpNotFoundException("create community is disabled");
        }
        if (userId == null || userId <= 0) {
            return Redirect.to("/login");
        }
        return pageViewFactory.newCreateCommunityView();
    }
}
