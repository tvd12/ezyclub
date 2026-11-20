package org.youngmonkeys.ezyclub.web.converter;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.util.EzyclubLevels;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubUserResponse;
import org.youngmonkeys.ezyplatform.model.MediaNameModel;
import org.youngmonkeys.ezyplatform.model.UserModel;
import org.youngmonkeys.ezyplatform.web.service.WebMediaService;
import org.youngmonkeys.ezyplatform.web.service.WebUserService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@EzySingleton
@AllArgsConstructor
public class WebEzyclubUserResponseFactory {

    private final WebUserService userService;
    private final WebMediaService mediaService;

    public Map<Long, WebEzyclubUserResponse> newUserResponseMap(
        Collection<Long> userIds,
        Map<Long, MemberModel> memberByUserId
    ) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserModel> users = userService.getUserListByIds(userIds);
        Set<Long> avatarIds = users
            .stream()
            .map(UserModel::getAvatarImageId)
            .filter(it -> it > 0)
            .collect(Collectors.toSet());
        Map<Long, MediaNameModel> avatarById = avatarIds.isEmpty()
            ? Collections.emptyMap()
            : mediaService.getMediaNameMapByIds(avatarIds);
        Map<Long, WebEzyclubUserResponse> answer = new HashMap<>();
        for (UserModel user : users) {
            MediaNameModel avatar = avatarById.get(user.getAvatarImageId());
            answer.put(
                user.getId(),
                newUserResponse(user, avatar, memberByUserId.get(user.getId()))
            );
        }
        return answer;
    }

    public WebEzyclubUserResponse newUserResponse(long userId, MemberModel member) {
        return newUserResponseMap(
            Collections.singletonList(userId),
            member == null
                ? Collections.emptyMap()
                : Collections.singletonMap(userId, member)
        ).get(userId);
    }

    private static WebEzyclubUserResponse newUserResponse(
        UserModel user,
        MediaNameModel avatar,
        MemberModel member
    ) {
        long points = member != null ? member.getPoints() : 0;
        String displayName = user.getDisplayName();
        return WebEzyclubUserResponse.builder()
            .uuid(user.getUuid())
            .username(user.getUsername())
            .displayName(
                displayName == null || displayName.trim().isEmpty()
                    ? user.getUsername()
                    : displayName
            )
            .avatarUrl(avatar != null ? "/api/v1/media/" + avatar.getName() : "")
            .points(points)
            .level(EzyclubLevels.levelOf(points))
            .role(member != null ? member.getRole() : "")
            .build();
    }
}
