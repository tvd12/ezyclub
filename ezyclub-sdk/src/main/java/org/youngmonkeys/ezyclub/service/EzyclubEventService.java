package org.youngmonkeys.ezyclub.service;

import com.tvd12.ezyfox.bean.annotation.EzyAutoBind;
import com.tvd12.ezyfox.util.Next;
import lombok.Setter;
import org.youngmonkeys.ezyclub.converter.EzyclubEntityToModelConverter;
import org.youngmonkeys.ezyclub.model.EventModel;
import org.youngmonkeys.ezyclub.model.SaveEventModel;
import org.youngmonkeys.ezyclub.repo.EzyclubEventRepository;
import org.youngmonkeys.ezycrm.entity.EzyCRMEvent;
import org.youngmonkeys.ezycrm.entity.EzyCRMEventParticipant;
import org.youngmonkeys.ezycrm.entity.EzyCRMEventParticipantStatus;
import org.youngmonkeys.ezycrm.entity.EzyCRMEventStatus;
import org.youngmonkeys.ezycrm.repo.EzyCRMEventParticipantRepository;
import org.youngmonkeys.ezyplatform.exception.ResourceNotFoundException;
import org.youngmonkeys.ezyplatform.time.ClockProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.EVENT_CATEGORY_COMMUNITY;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.EVENT_TYPE_COMMUNITY_EVENT;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.ORGANIZER_TYPE_USER;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.PARTICIPANT_TYPE_USER;

@Setter
public class EzyclubEventService {

    @EzyAutoBind
    protected EzyclubEventRepository eventRepository;

    @EzyAutoBind
    protected EzyCRMEventParticipantRepository participantRepository;

    @EzyAutoBind
    protected EzyclubEntityToModelConverter entityToModelConverter;

    @EzyAutoBind
    protected ClockProxy clock;

    private static final int MAX_EVENTS_PER_RANGE = 500;

    public long addEvent(long communityId, long creatorUserId, SaveEventModel model) {
        LocalDateTime now = clock.nowDateTime();
        EzyCRMEvent entity = new EzyCRMEvent();
        entity.setParentId(communityId);
        entity.setEventType(EVENT_TYPE_COMMUNITY_EVENT);
        entity.setCategoryName(EVENT_CATEGORY_COMMUNITY);
        entity.setOrganizerType(ORGANIZER_TYPE_USER);
        entity.setOrganizerId(creatorUserId);
        entity.setStatus(EzyCRMEventStatus.PUBLISHED.toString());
        entity.setOnlineLinkPassword("");
        entity.setCreatedAt(now);
        mergeEvent(entity, model);
        eventRepository.save(entity);
        return entity.getId();
    }

    public void updateEvent(long eventId, SaveEventModel model) {
        EzyCRMEvent entity = getEventEntityOrThrow(eventId);
        mergeEvent(entity, model);
        eventRepository.save(entity);
    }

    public void removeEvent(long eventId) {
        EzyCRMEvent entity = getEventEntityOrThrow(eventId);
        entity.setStatus(EzyCRMEventStatus.DELETED.toString());
        entity.setUpdatedAt(clock.nowDateTime());
        eventRepository.save(entity);
    }

    public EventModel getEventById(long eventId) {
        EzyCRMEvent entity = eventRepository.findById(eventId);
        if (entity == null || !EVENT_TYPE_COMMUNITY_EVENT.equals(entity.getEventType())) {
            return null;
        }
        return entityToModelConverter.toModel(entity);
    }

    public List<EventModel> getPublishedEvents(
        long communityId,
        long fromTime,
        long toTime
    ) {
        return eventRepository
            .findByParentIdAndEventTypeAndStatusAndStartAtBetween(
                communityId,
                EVENT_TYPE_COMMUNITY_EVENT,
                EzyCRMEventStatus.PUBLISHED.toString(),
                clock.toLocalDateTime(fromTime),
                clock.toLocalDateTime(toTime),
                Next.limit(MAX_EVENTS_PER_RANGE)
            )
            .stream()
            .map(entityToModelConverter::toModel)
            .collect(Collectors.toList());
    }

    public boolean updateAttending(long eventId, long userId, boolean attending) {
        LocalDateTime now = clock.nowDateTime();
        EzyCRMEventParticipant entity = participantRepository
            .findByEventIdAndParticipantTypeAndParticipantId(
                eventId,
                PARTICIPANT_TYPE_USER,
                userId
            );
        if (entity == null) {
            if (!attending) {
                return false;
            }
            entity = new EzyCRMEventParticipant();
            entity.setEventId(eventId);
            entity.setParticipantType(PARTICIPANT_TYPE_USER);
            entity.setParticipantId(userId);
            entity.setScores(BigDecimal.ZERO);
            entity.setCheckInStatus("NOT_CHECKED_IN");
            entity.setCreatedAt(now);
        }
        entity.setStatus(
            attending
                ? EzyCRMEventParticipantStatus.ADDED.toString()
                : EzyCRMEventParticipantStatus.REMOVED.toString()
        );
        entity.setUpdatedAt(now);
        participantRepository.save(entity);
        return attending;
    }

    public List<Long> getAttendeeUserIds(long eventId) {
        return participantRepository
            .findByEventIdAndParticipantType(eventId, PARTICIPANT_TYPE_USER)
            .stream()
            .filter(it -> EzyCRMEventParticipantStatus.ADDED.toString().equals(it.getStatus()))
            .map(EzyCRMEventParticipant::getParticipantId)
            .collect(Collectors.toList());
    }

    protected void mergeEvent(EzyCRMEvent entity, SaveEventModel model) {
        entity.setTitle(model.getTitle());
        entity.setSubtitle(model.getLocation() == null ? "" : model.getLocation());
        entity.setNotes(model.getDescription());
        entity.setOnlineLink(model.getMeetingUrl() == null ? "" : model.getMeetingUrl());
        entity.setStartAt(clock.toLocalDateTime(model.getStartAt()));
        entity.setFinishAt(clock.toLocalDateTime(model.getFinishAt()));
        entity.setUpdatedAt(clock.nowDateTime());
    }

    protected EzyCRMEvent getEventEntityOrThrow(long eventId) {
        EzyCRMEvent entity = eventRepository.findById(eventId);
        if (entity == null || !EVENT_TYPE_COMMUNITY_EVENT.equals(entity.getEventType())) {
            throw new ResourceNotFoundException("event");
        }
        return entity;
    }
}
