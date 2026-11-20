package org.youngmonkeys.ezyclub.admin.validator;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import com.tvd12.ezyhttp.core.exception.HttpBadRequestException;
import org.youngmonkeys.ezyclub.admin.model.AdminEzyclubSettings;

import java.util.HashMap;
import java.util.Map;

@EzySingleton
public class AdminEzyclubSettingsValidator {

    private static final String ERROR_INVALID = "invalid";
    private static final String ERROR_TOO_LONG = "tooLong";
    private static final String COLOR_PATTERN = "^#[0-9a-fA-F]{6}$";
    private static final int MAX_ANNOUNCEMENT_LENGTH = 200;
    private static final int MAX_PAGE_SIZE = 50;

    public void validate(AdminEzyclubSettings settings) {
        settings.fillMissingParts();
        Map<String, String> errors = new HashMap<>();
        String primaryColor = settings.getPrimaryColor();
        if (primaryColor == null || !primaryColor.matches(COLOR_PATTERN)) {
            errors.put("primaryColor", ERROR_INVALID);
        }
        validateRange(errors, "communitiesPerPage", settings.getCommunitiesPerPage());
        validateRange(errors, "postsPerPage", settings.getPostsPerPage());
        String text = settings.getAnnouncement().getText();
        if (text != null && text.length() > MAX_ANNOUNCEMENT_LENGTH) {
            errors.put("announcementText", ERROR_TOO_LONG);
        }
        if (!errors.isEmpty()) {
            throw new HttpBadRequestException(errors);
        }
    }

    private static void validateRange(Map<String, String> errors, String field, int value) {
        if (value < 1 || value > MAX_PAGE_SIZE) {
            errors.put(field, ERROR_INVALID);
        }
    }
}
