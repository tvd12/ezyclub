package org.youngmonkeys.ezyclub.admin.controller.view;

import org.youngmonkeys.ezyclub.admin.service.AdminEzyclubSettingService;
import com.tvd12.ezyfox.annotation.EzyFeature;
import com.tvd12.ezyhttp.server.core.annotation.Authenticated;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.view.View;
import lombok.AllArgsConstructor;

@Controller
@Authenticated
@EzyFeature("settings_management")
@AllArgsConstructor
public class AdminSettingsController {

    private final AdminEzyclubSettingService settingService;

    @DoGet("/settings")
    public View settingsGet() {
        return View.builder()
            .template("ezyclub/settings")
            .addVariable("settings", settingService.getSettings())
            .addVariable("webUrl", settingService.getWebUrl())
            .addVariable("enableSaveButton", Boolean.TRUE)
            .build();
    }
}
