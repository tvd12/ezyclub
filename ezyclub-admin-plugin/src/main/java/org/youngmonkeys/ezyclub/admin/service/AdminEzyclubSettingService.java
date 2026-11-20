package org.youngmonkeys.ezyclub.admin.service;

import org.youngmonkeys.ezyclub.admin.model.AdminEzyclubSettings;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.admin.service.AdminSettingService;

import static org.youngmonkeys.ezyclub.admin.model.AdminEzyclubSettings.SETTING_NAME;

@EzySingleton
@AllArgsConstructor
public class AdminEzyclubSettingService {

    private final AdminSettingService settingService;

    public AdminEzyclubSettings getSettings() {
        return settingService.getObjectValue(
            SETTING_NAME,
            AdminEzyclubSettings.class,
            new AdminEzyclubSettings()
        );
    }

    public void saveSettings(AdminEzyclubSettings settings) {
        settingService.setObjectValue(SETTING_NAME, settings);
        settingService.setLastUpdateTime(SETTING_NAME);
    }

    public String getWebUrl() {
        return settingService.getWebUrl();
    }

    public String getWebsocketUrl() {
        return settingService.getWebsocketUrl();
    }
}
