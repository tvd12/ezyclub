package org.youngmonkeys.ezyclub.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebEzyclubSettings {

    public static final String SETTING_NAME = "ezyclub_settings";
    public static final String DEFAULT_PRIMARY_COLOR = "#0f766e";
    private static final String COLOR_PATTERN = "^#[0-9a-fA-F]{6}$";

    private String primaryColor = DEFAULT_PRIMARY_COLOR;
    private boolean showLanguageSwitcher = true;
    private boolean allowUserCreateCommunity = true;
    private int communitiesPerPage = 12;
    private int postsPerPage = 10;
    private Announcement announcement = new Announcement();

    @JsonIgnore
    public String getValidPrimaryColor() {
        return primaryColor != null && primaryColor.matches(COLOR_PATTERN)
            ? primaryColor
            : DEFAULT_PRIMARY_COLOR;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Announcement {
        private boolean enabled;
        private String text = "";
    }
}
