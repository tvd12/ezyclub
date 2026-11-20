package org.youngmonkeys.ezyclub.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tvd12.ezyfox.bean.annotation.EzyPostInit;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.web.model.WebEzyclubSettings;
import org.youngmonkeys.ezyplatform.service.SettingService;

import static org.youngmonkeys.ezyclub.web.model.WebEzyclubSettings.SETTING_NAME;

@EzySingleton
@AllArgsConstructor
public class WebEzyclubSettingService {

    private final SettingService settingService;
    private final ObjectMapper objectMapper;

    private static final int CACHE_PERIOD_IN_SECOND = 3;
    private static final WebEzyclubSettings DEFAULT_SETTINGS =
        new WebEzyclubSettings();

    @EzyPostInit
    public void postInit() {
        settingService.addValueConverter(
            SETTING_NAME,
            value -> objectMapper.readValue(
                value,
                WebEzyclubSettings.class
            )
        );
        settingService.scheduleCacheValue(
            SETTING_NAME,
            CACHE_PERIOD_IN_SECOND
        );
    }

    public WebEzyclubSettings getSettings() {
        return settingService.getCachedValue(
            SETTING_NAME,
            DEFAULT_SETTINGS
        );
    }
}
