package org.youngmonkeys.ezyclub.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminEzyclubSettings {

    public static final String SETTING_NAME = "ezyclub_settings";
    public static final String DEFAULT_PRIMARY_COLOR = "#0f766e";

    private String primaryColor = DEFAULT_PRIMARY_COLOR;
    private boolean showLanguageSwitcher = true;
    private boolean allowUserCreateCommunity = true;
    private int communitiesPerPage = 12;
    private int postsPerPage = 10;
    private Announcement announcement = new Announcement();

    public void fillMissingParts() {
        if (announcement == null) {
            announcement = new Announcement();
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Announcement {
        private boolean enabled;
        private String text = "";
    }
}
