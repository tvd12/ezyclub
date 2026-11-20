package org.youngmonkeys.ezyclub.web.validator;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyclub.constant.CommunityCategory;
import org.youngmonkeys.ezyclub.constant.CommunityPrivacy;
import org.youngmonkeys.ezyclub.util.EzyclubLevels;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubAddCommentRequest;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveCommunityRequest;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveCourseRequest;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveEventRequest;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveFeedPostRequest;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveLessonRequest;
import org.youngmonkeys.ezyplatform.exception.BadRequestException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@EzySingleton
public class WebEzyclubValidator {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final Pattern COLOR_PATTERN = Pattern.compile("^#[0-9a-fA-F]{6}$");
    private static final int MAX_NAME_LENGTH = 120;
    private static final int MAX_SLUG_LENGTH = 80;
    private static final int MAX_SHORT_TEXT_LENGTH = 300;
    private static final int MAX_LONG_TEXT_LENGTH = 20000;
    private static final int MAX_POST_CATEGORIES = 10;
    private static final int MAX_POST_CATEGORY_LENGTH = 30;
    private static final int MAX_URL_LENGTH = 250;
    private static final int MAX_PAGE_SIZE = 50;

    public void validate(WebEzyclubSaveCommunityRequest request, boolean creating) {
        Map<String, String> errors = new HashMap<>();
        if (creating) {
            String slug = request.getSlug();
            if (isBlank(slug)) {
                errors.put("slug", "required");
            } else if (slug.length() > MAX_SLUG_LENGTH
                || !SLUG_PATTERN.matcher(slug).matches()
            ) {
                errors.put("slug", "invalid");
            }
        }
        validateText(errors, "name", request.getName(), MAX_NAME_LENGTH, true);
        validateText(
            errors,
            "shortDescription",
            request.getShortDescription(),
            MAX_SHORT_TEXT_LENGTH,
            false
        );
        validateText(
            errors,
            "description",
            request.getDescription(),
            MAX_LONG_TEXT_LENGTH,
            false
        );
        if (CommunityCategory.of(request.getCategory()) == null) {
            errors.put("category", "invalid");
        }
        if (!isEnumValue(CommunityPrivacy.values(), request.getPrivacy())) {
            errors.put("privacy", "invalid");
        }
        if (!isBlank(request.getThemeColor())
            && !COLOR_PATTERN.matcher(request.getThemeColor()).matches()
        ) {
            errors.put("themeColor", "invalid");
        }
        List<String> postCategories = request.getPostCategories();
        if (postCategories != null) {
            if (postCategories.size() > MAX_POST_CATEGORIES) {
                errors.put("postCategories", "tooMany");
            }
            for (String category : postCategories) {
                if (isBlank(category)
                    || category.length() > MAX_POST_CATEGORY_LENGTH
                    || category.contains(",")
                ) {
                    errors.put("postCategories", "invalid");
                }
            }
        }
        throwIfHasErrors(errors);
    }

    public void validate(WebEzyclubSaveFeedPostRequest request, List<String> categories) {
        Map<String, String> errors = new HashMap<>();
        validateText(errors, "title", request.getTitle(), MAX_SHORT_TEXT_LENGTH, true);
        validateText(errors, "content", request.getContent(), MAX_LONG_TEXT_LENGTH, true);
        String category = request.getCategory();
        if (!isBlank(category) && !categories.contains(category)) {
            errors.put("category", "invalid");
        }
        throwIfHasErrors(errors);
    }

    public void validate(WebEzyclubAddCommentRequest request) {
        Map<String, String> errors = new HashMap<>();
        validateText(errors, "content", request.getContent(), MAX_LONG_TEXT_LENGTH, true);
        throwIfHasErrors(errors);
    }

    public void validate(WebEzyclubSaveCourseRequest request) {
        Map<String, String> errors = new HashMap<>();
        validateText(errors, "title", request.getTitle(), MAX_SHORT_TEXT_LENGTH, true);
        validateText(
            errors,
            "description",
            request.getDescription(),
            MAX_LONG_TEXT_LENGTH,
            false
        );
        if (request.getUnlockLevel() < 1
            || request.getUnlockLevel() > EzyclubLevels.maxLevel()
        ) {
            errors.put("unlockLevel", "invalid");
        }
        throwIfHasErrors(errors);
    }

    public void validate(WebEzyclubSaveLessonRequest request) {
        Map<String, String> errors = new HashMap<>();
        validateText(errors, "title", request.getTitle(), MAX_SHORT_TEXT_LENGTH, true);
        validateText(errors, "content", request.getContent(), MAX_LONG_TEXT_LENGTH, false);
        validateUrl(errors, "videoUrl", request.getVideoUrl());
        throwIfHasErrors(errors);
    }

    public void validate(WebEzyclubSaveEventRequest request) {
        Map<String, String> errors = new HashMap<>();
        validateText(errors, "title", request.getTitle(), MAX_SHORT_TEXT_LENGTH, true);
        validateText(errors, "location", request.getLocation(), MAX_SHORT_TEXT_LENGTH, false);
        validateText(
            errors,
            "description",
            request.getDescription(),
            MAX_LONG_TEXT_LENGTH,
            false
        );
        validateUrl(errors, "meetingUrl", request.getMeetingUrl());
        if (request.getStartAt() <= 0) {
            errors.put("startAt", "required");
        }
        if (request.getFinishAt() < request.getStartAt()) {
            errors.put("finishAt", "invalid");
        }
        throwIfHasErrors(errors);
    }

    public void validatePageSize(int limit) {
        if (limit < 1 || limit > MAX_PAGE_SIZE) {
            throw new BadRequestException(
                Collections.singletonMap("limit", "invalid")
            );
        }
    }

    private static void validateText(
        Map<String, String> errors,
        String field,
        String value,
        int maxLength,
        boolean required
    ) {
        if (isBlank(value)) {
            if (required) {
                errors.put(field, "required");
            }
        } else if (value.length() > maxLength) {
            errors.put(field, "tooLong");
        }
    }

    private static void validateUrl(Map<String, String> errors, String field, String value) {
        if (isBlank(value)) {
            return;
        }
        if (value.length() > MAX_URL_LENGTH
            || !(value.startsWith("https://") || value.startsWith("http://"))
        ) {
            errors.put(field, "invalid");
        }
    }

    private static boolean isEnumValue(Enum<?>[] values, String value) {
        for (Enum<?> item : values) {
            if (item.toString().equals(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void throwIfHasErrors(Map<String, String> errors) {
        if (!errors.isEmpty()) {
            throw new BadRequestException(errors);
        }
    }
}
