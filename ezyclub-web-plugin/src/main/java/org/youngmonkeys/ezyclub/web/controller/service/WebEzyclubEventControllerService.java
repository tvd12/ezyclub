package org.youngmonkeys.ezyclub.web.controller.service;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.EventModel;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.model.SaveEventModel;
import org.youngmonkeys.ezyclub.service.EzyclubMemberService;
import org.youngmonkeys.ezyclub.web.request.WebEzyclubSaveEventRequest;
import org.youngmonkeys.ezyclub.web.response.WebEzyclubEventResponse;
import org.youngmonkeys.ezyclub.web.service.WebEzyclubEventService;
import org.youngmonkeys.ezycrm.entity.EzyCRMEventStatus;
import org.youngmonkeys.ezyplatform.exception.BadRequestException;
import org.youngmonkeys.ezyplatform.exception.ResourceNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@EzySingleton
@AllArgsConstructor
public class WebEzyclubEventControllerService {

    private final WebEzyclubEventService eventService;

    private static final long MAX_RANGE_MILLIS = 100L * 24 * 60 * 60 * 1000;

    public long addEvent(
        CommunityModel community,
        long userId,
        WebEzyclubSaveEventRequest request
    ) {
        return eventService.addEvent(community.getId(), userId, toSaveModel(request));
    }

    public void updateEvent(EventModel event, WebEzyclubSaveEventRequest request) {
        eventService.updateEvent(event.getId(), toSaveModel(request));
    }

    public EventModel getEventInCommunity(CommunityModel community, long eventId) {
        EventModel event = eventService.getEventById(eventId);
        if (event == null
            || event.getCommunityId() != community.getId()
            || EzyCRMEventStatus.DELETED.toString().equals(event.getStatus())
        ) {
            throw new ResourceNotFoundException("event");
        }
        return event;
    }

    public List<WebEzyclubEventResponse> getEvents(
        CommunityModel community,
        MemberModel member,
        long fromTime,
        long toTime
    ) {
        if (toTime <= fromTime || toTime - fromTime > MAX_RANGE_MILLIS) {
            throw new BadRequestException(Collections.singletonMap("toTime", "invalid"));
        }
        boolean manager = EzyclubMemberService.isManager(member);
        long userId = member != null ? member.getUserId() : 0;
        return eventService
            .getPublishedEvents(community.getId(), fromTime, toTime)
            .stream()
            .map(it -> {
                List<Long> attendeeIds = eventService.getAttendeeUserIds(it.getId());
                return WebEzyclubEventResponse.builder()
                    .id(it.getId())
                    .title(it.getTitle())
                    .location(it.getLocation())
                    .description(it.getDescription())
                    .meetingUrl(member != null ? it.getMeetingUrl() : "")
                    .startAt(it.getStartAt())
                    .finishAt(it.getFinishAt())
                    .attendeeCount(attendeeIds.size())
                    .attending(attendeeIds.contains(userId))
                    .editable(manager)
                    .build();
            })
            .collect(Collectors.toList());
    }

    public boolean updateAttending(EventModel event, MemberModel member, boolean value) {
        return eventService.updateAttending(event.getId(), member.getUserId(), value);
    }

    private static SaveEventModel toSaveModel(WebEzyclubSaveEventRequest request) {
        return SaveEventModel.builder()
            .title(request.getTitle().trim())
            .location(request.getLocation() == null ? "" : request.getLocation().trim())
            .description(request.getDescription() == null ? "" : request.getDescription())
            .meetingUrl(request.getMeetingUrl() == null ? "" : request.getMeetingUrl().trim())
            .startAt(request.getStartAt())
            .finishAt(request.getFinishAt())
            .build();
    }
}
