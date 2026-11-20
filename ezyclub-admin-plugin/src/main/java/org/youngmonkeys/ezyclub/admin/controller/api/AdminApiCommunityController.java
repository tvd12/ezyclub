package org.youngmonkeys.ezyclub.admin.controller.api;

import com.tvd12.ezyfox.annotation.EzyFeature;
import com.tvd12.ezyhttp.core.exception.HttpBadRequestException;
import com.tvd12.ezyhttp.core.response.ResponseEntity;
import com.tvd12.ezyhttp.server.core.annotation.Api;
import com.tvd12.ezyhttp.server.core.annotation.Authenticated;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.annotation.DoPut;
import com.tvd12.ezyhttp.server.core.annotation.PathVariable;
import com.tvd12.ezyhttp.server.core.annotation.RequestParam;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.admin.response.AdminEzyclubCommunityResponse;
import org.youngmonkeys.ezyclub.admin.service.AdminEzyclubCommunityService;
import org.youngmonkeys.ezyclub.admin.service.AdminEzyclubSettingService;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.EzyclubPageModel;
import org.youngmonkeys.ezycrm.entity.UserGroupStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Api
@Authenticated
@Controller("/api/v1")
@EzyFeature("ezyclub")
@AllArgsConstructor
public class AdminApiCommunityController {

    private final AdminEzyclubCommunityService communityService;
    private final AdminEzyclubSettingService settingService;

    private static final int MAX_PAGE_SIZE = 100;
    private static final List<String> STATUSES = Arrays.asList(
        UserGroupStatus.ACTIVATED.toString(),
        UserGroupStatus.INACTIVATED.toString(),
        UserGroupStatus.DELETED.toString()
    );

    @DoGet("/communities")
    public EzyclubPageModel<AdminEzyclubCommunityResponse> communitiesGet(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        if (limit < 1 || limit > MAX_PAGE_SIZE) {
            throw new HttpBadRequestException(Collections.singletonMap("limit", "invalid"));
        }
        String webUrl = settingService.getWebUrl();
        return communityService
            .getCommunityPage(Math.max(0, page), limit)
            .map(it -> toResponse(it, webUrl));
    }

    @DoPut("/communities/{communityId}/status/{status}")
    public ResponseEntity communityStatusPut(
        @PathVariable("communityId") long communityId,
        @PathVariable("status") String status
    ) {
        if (!STATUSES.contains(status)) {
            throw new HttpBadRequestException(Collections.singletonMap("status", "invalid"));
        }
        communityService.updateCommunityStatus(communityId, status);
        return ResponseEntity.noContent();
    }

    private static AdminEzyclubCommunityResponse toResponse(
        CommunityModel community,
        String webUrl
    ) {
        return AdminEzyclubCommunityResponse.builder()
            .id(community.getId())
            .slug(community.getSlug())
            .name(community.getName())
            .category(community.getCategory())
            .privacy(community.getPrivacy())
            .status(community.getStatus())
            .memberCount(community.getMemberCount())
            .createdAt(community.getCreatedAt())
            .url((webUrl == null ? "" : webUrl) + "/c/" + community.getSlug())
            .build();
    }
}
