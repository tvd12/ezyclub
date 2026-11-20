package org.youngmonkeys.ezyclub.admin.controller.api;

import org.youngmonkeys.ezyclub.admin.model.AdminEzyclubSettings;
import org.youngmonkeys.ezyclub.admin.service.AdminEzyclubSettingService;
import org.youngmonkeys.ezyclub.admin.validator.AdminEzyclubSettingsValidator;
import com.tvd12.ezyfox.annotation.EzyFeature;
import com.tvd12.ezyhttp.core.response.ResponseEntity;
import com.tvd12.ezyhttp.server.core.annotation.Api;
import com.tvd12.ezyhttp.server.core.annotation.Authenticated;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.annotation.DoPut;
import com.tvd12.ezyhttp.server.core.annotation.RequestBody;
import lombok.AllArgsConstructor;

@Api
@Authenticated
@Controller("/api/v1")
@EzyFeature("settings_management")
@AllArgsConstructor
public class AdminApiSettingController {

    private final AdminEzyclubSettingService settingService;
    private final AdminEzyclubSettingsValidator settingsValidator;

    @DoGet("/settings")
    public AdminEzyclubSettings settingsGet() {
        return settingService.getSettings();
    }

    @DoPut("/settings")
    public ResponseEntity settingsPut(
        @RequestBody AdminEzyclubSettings request
    ) {
        settingsValidator.validate(request);
        settingService.saveSettings(request);
        return ResponseEntity.noContent();
    }
}
