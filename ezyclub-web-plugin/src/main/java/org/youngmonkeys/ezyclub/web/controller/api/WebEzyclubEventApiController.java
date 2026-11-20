package org.youngmonkeys.ezyclub.web.controller.api;

import com.tvd12.ezyhttp.core.response.ResponseEntity;
import com.tvd12.ezyhttp.server.core.annotation.Api;
import com.tvd12.ezyhttp.server.core.annotation.Authenticatable;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoDelete;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.annotation.DoPost;
import com.tvd12.ezyhttp.server.core.annotation.DoPut;
import com.tvd12.ezyhttp.server.core.annotation.PathVariable;
import com.tvd12.ezyhttp.server.core.annotation.RequestBody;
import com.tvd12.ezyhttp.server.core.annotation.RequestParam;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.EventModel;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.web.controller.service.WebEzyclubAccessControllerService;
import org.youngmonkeys.ezyclub.web.controller.service.WebEzyclubEventControllerService;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveEventRequest;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubUpdateBooleanRequest;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubEventResponse;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubEventService;
import org.youngmonkeys.ezyclub.web.validator.WebEzyclubValidator;
import org.youngmonkeys.ezyplatform.web.annotation.UserId;

import java.util.List;

@Api
@Authenticatable
@Controller("/api/v1/ezyclub")
@AllArgsConstructor
public class WebEzyclubEventApiController {

    private final WebEzyclubAccessControllerService accessControllerService;
    private final WebEzyclubEventControllerService eventControllerService;
    private final WebEzyclubEventService eventService;
    private final WebEzyclubValidator validator;

    @DoGet("/communities/{communityId}/events")
    public List<WebEzyclubEventResponse> eventsGet(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @RequestParam("fromTime") long fromTime,
        @RequestParam("toTime") long toTime
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateCanView(community, userId);
        return eventControllerService.getEvents(community, member, fromTime, toTime);
    }

    @DoPost("/communities/{communityId}/events")
    public ResponseEntity eventsPost(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @RequestBody WebEzyclubSaveEventRequest request
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateManager(community, userId);
        validator.validate(request);
        eventControllerService.addEvent(community, member.getUserId(), request);
        return ResponseEntity.noContent();
    }

    @DoPut("/communities/{communityId}/events/{eventId}")
    public ResponseEntity eventPut(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("eventId") long eventId,
        @RequestBody WebEzyclubSaveEventRequest request
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        accessControllerService.validateManager(community, userId);
        EventModel event = eventControllerService.getEventInCommunity(community, eventId);
        validator.validate(request);
        eventControllerService.updateEvent(event, request);
        return ResponseEntity.noContent();
    }

    @DoDelete("/communities/{communityId}/events/{eventId}")
    public ResponseEntity eventDelete(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("eventId") long eventId
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        accessControllerService.validateManager(community, userId);
        eventControllerService.getEventInCommunity(community, eventId);
        eventService.removeEvent(eventId);
        return ResponseEntity.noContent();
    }

    @DoPut("/communities/{communityId}/events/{eventId}/attending")
    public ResponseEntity eventAttendingPut(
        @UserId Long userId,
        @PathVariable("communityId") long communityId,
        @PathVariable("eventId") long eventId,
        @RequestBody WebEzyclubUpdateBooleanRequest request
    ) {
        CommunityModel community = accessControllerService.getCommunityById(communityId);
        MemberModel member = accessControllerService.validateMember(community, userId);
        EventModel event = eventControllerService.getEventInCommunity(community, eventId);
        eventControllerService.updateAttending(event, member, request.isValue());
        return ResponseEntity.noContent();
    }
}
