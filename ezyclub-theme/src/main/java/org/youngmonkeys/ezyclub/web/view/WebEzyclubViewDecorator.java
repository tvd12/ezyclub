package org.youngmonkeys.ezyclub.web.view;

import org.youngmonkeys.ezyclub.web.service.WebEzyclubSettingService;
import com.tvd12.ezyfox.bean.annotation.EzyAutoBind;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import com.tvd12.ezyhttp.server.core.view.View;
import lombok.Setter;
import org.youngmonkeys.ezyplatform.web.controller.service.WebMessageControllerService;
import org.youngmonkeys.ezyplatform.web.view.WebViewDecorator;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.youngmonkeys.ezyplatform.constant.CommonConstants.VIEW_VARIABLE_ADDITIONAL_MESSAGE_MAP;

@Setter
@EzySingleton
public class WebEzyclubViewDecorator extends WebViewDecorator {

    @EzyAutoBind
    private WebEzyclubSettingService themeSettingService;

    @EzyAutoBind
    private WebMessageControllerService messageControllerService;

    public static final String VIEW_VARIABLE_THEME_SETTINGS = "themeSettings";

    private static final Set<String> JS_MESSAGE_KEYS = Collections.unmodifiableSet(
        new HashSet<>(
            Arrays.asList(
                "attendees_count",
                "cancel",
                "change",
                "completed",
                "confirm",
                "confirm_delete_comment",
                "confirm_delete_course",
                "confirm_delete_event",
                "confirm_delete_lesson",
                "confirm_delete_post",
                "confirm_leave_community",
                "content",
                "course_not_found",
                "days_ago",
                "default_category_general",
                "default_category_questions",
                "default_category_wins",
                "delete",
                "deleted",
                "description",
                "display_order",
                "draft",
                "earn_points_hint",
                "edit_course",
                "edit_event",
                "edit_lesson",
                "edit_post",
                "ends_at",
                "error_duplicated",
                "error_invalid",
                "error_required",
                "error_too_long",
                "error_too_many",
                "going",
                "hours_ago",
                "join_to_earn_points",
                "join_to_participate",
                "joined_community",
                "just_now",
                "level_number",
                "location",
                "mark_as_complete",
                "max_level_reached",
                "meeting_url",
                "minutes_ago",
                "new_course",
                "new_event",
                "new_lesson",
                "new_post",
                "no_communities_found",
                "no_courses_yet",
                "no_lessons_yet",
                "no_members_yet",
                "no_posts_yet",
                "no_upcoming_events",
                "not_going",
                "permission_denied",
                "pin",
                "pinned",
                "points_from",
                "points_to_level_up",
                "post",
                "private",
                "progress_complete",
                "public",
                "published",
                "rank_number",
                "reply",
                "role_admin",
                "role_member",
                "role_owner",
                "save",
                "saved",
                "select_category",
                "something_went_wrong",
                "starts_at",
                "title",
                "unlock_at_level",
                "unlock_level",
                "unpin",
                "upload",
                "video_url",
                "write_reply",
                "write_something",
                "your_comment"
            )
        )
    );

    @Override
    public void decorate(HttpServletRequest request, View view) {
        super.decorate(request, view);
        view.setVariable(
            VIEW_VARIABLE_THEME_SETTINGS,
            themeSettingService.getSettings()
        );
        Map<String, Object> messages = new HashMap<>(
            messageControllerService.getMessageMapByKey(request, JS_MESSAGE_KEYS)
        );
        view.putKeyValuesToVariable(
            VIEW_VARIABLE_ADDITIONAL_MESSAGE_MAP,
            messages
        );
    }

    @Override
    protected boolean includeWebLanguages() {
        return true;
    }
}
