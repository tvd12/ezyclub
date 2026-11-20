package org.youngmonkeys.ezyclub.service;

import com.tvd12.ezyfox.bean.annotation.EzyAutoBind;
import com.tvd12.ezyfox.util.Next;
import lombok.Setter;
import org.youngmonkeys.ezyclub.constant.MemberRole;
import org.youngmonkeys.ezyclub.converter.EzyclubEntityToModelConverter;
import org.youngmonkeys.ezyclub.model.EzyclubPageModel;
import org.youngmonkeys.ezyclub.model.MemberModel;
import org.youngmonkeys.ezyclub.repo.EzyclubMemberRepository;
import org.youngmonkeys.ezycrm.entity.UserGroupMember;
import org.youngmonkeys.ezycrm.entity.UserGroupMemberStatus;
import org.youngmonkeys.ezyplatform.exception.ResourceNotFoundException;
import org.youngmonkeys.ezyplatform.time.ClockProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.USER_TYPE_USER;
import static org.youngmonkeys.ezyclub.service.EzyclubCommunityService.MEMBER_NEVER_EXPIRED_AT;

@Setter
public class EzyclubMemberService {

    @EzyAutoBind
    protected EzyclubMemberRepository memberRepository;

    @EzyAutoBind
    protected EzyclubEntityToModelConverter entityToModelConverter;

    @EzyAutoBind
    protected ClockProxy clock;

    public MemberModel joinCommunity(long communityId, long userId) {
        LocalDateTime now = clock.nowDateTime();
        UserGroupMember entity = memberRepository.findByUserGroupIdAndUserTypeAndUserId(
            communityId,
            USER_TYPE_USER,
            userId
        );
        if (entity == null) {
            entity = new UserGroupMember();
            entity.setUserGroupId(communityId);
            entity.setUserType(USER_TYPE_USER);
            entity.setUserId(userId);
            entity.setMemberType(MemberRole.MEMBER.toString());
            entity.setScores(BigDecimal.ZERO);
            entity.setExpiredAt(MEMBER_NEVER_EXPIRED_AT);
            entity.setCreatedAt(now);
        }
        entity.setStatus(UserGroupMemberStatus.ACTIVATED.toString());
        entity.setUpdatedAt(now);
        memberRepository.save(entity);
        return entityToModelConverter.toModel(entity);
    }

    public void leaveCommunity(long communityId, long userId) {
        UserGroupMember entity = getMemberEntityOrThrow(communityId, userId);
        entity.setStatus(UserGroupMemberStatus.REMOVED.toString());
        entity.setUpdatedAt(clock.nowDateTime());
        memberRepository.save(entity);
    }

    public void updateMemberRole(long communityId, long userId, MemberRole role) {
        UserGroupMember entity = getMemberEntityOrThrow(communityId, userId);
        entity.setMemberType(role.toString());
        entity.setUpdatedAt(clock.nowDateTime());
        memberRepository.save(entity);
    }

    public void increasePoints(long communityId, long userId, long points) {
        memberRepository.updateToIncreaseScores(
            communityId,
            USER_TYPE_USER,
            userId,
            BigDecimal.valueOf(points),
            clock.nowDateTime()
        );
    }

    public MemberModel getMember(long communityId, long userId) {
        if (userId <= 0) {
            return null;
        }
        return entityToModelConverter.toModel(
            memberRepository.findByUserGroupIdAndUserTypeAndUserId(
                communityId,
                USER_TYPE_USER,
                userId
            )
        );
    }

    public MemberModel getActivatedMember(long communityId, long userId) {
        MemberModel member = getMember(communityId, userId);
        return member != null && isActivated(member) ? member : null;
    }

    public Map<Long, MemberModel> getMemberMapByUserIds(
        long communityId,
        Collection<Long> userIds
    ) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return memberRepository
            .findByUserGroupIdAndUserTypeAndUserIdIn(
                communityId,
                USER_TYPE_USER,
                userIds
            )
            .stream()
            .map(entityToModelConverter::toModel)
            .collect(Collectors.toMap(MemberModel::getUserId, Function.identity()));
    }

    public EzyclubPageModel<MemberModel> getMemberPage(
        long communityId,
        int page,
        int limit
    ) {
        List<UserGroupMember> entities = memberRepository
            .findByUserGroupIdAndUserTypeAndStatusOrderByIdDesc(
                communityId,
                USER_TYPE_USER,
                UserGroupMemberStatus.ACTIVATED.toString(),
                Next.fromSkipLimit((long) page * limit, limit + 1)
            );
        return EzyclubPageModel.of(toModels(entities), page, limit);
    }

    public List<MemberModel> getTopMembers(long communityId, int limit) {
        return toModels(
            memberRepository.findByUserGroupIdAndUserTypeAndStatusOrderByScoresDesc(
                communityId,
                USER_TYPE_USER,
                UserGroupMemberStatus.ACTIVATED.toString(),
                Next.limit(limit)
            )
        );
    }

    public long getMemberRank(long communityId, long points) {
        return memberRepository.countByUserGroupIdAndUserTypeAndStatusAndScoresGt(
            communityId,
            USER_TYPE_USER,
            UserGroupMemberStatus.ACTIVATED.toString(),
            BigDecimal.valueOf(points)
        ) + 1;
    }

    public List<MemberModel> getActivatedMembershipsOfUser(long userId, int limit) {
        return toModels(
            memberRepository.findByUserTypeAndUserIdAndStatusOrderByIdDesc(
                USER_TYPE_USER,
                userId,
                UserGroupMemberStatus.ACTIVATED.toString(),
                Next.limit(limit)
            )
        );
    }

    public static boolean isActivated(MemberModel member) {
        return UserGroupMemberStatus.ACTIVATED.toString().equals(member.getStatus());
    }

    public static boolean isManager(MemberModel member) {
        return member != null
            && isActivated(member)
            && MemberRole.of(member.getRole()).isManager();
    }

    protected UserGroupMember getMemberEntityOrThrow(long communityId, long userId) {
        UserGroupMember entity = memberRepository.findByUserGroupIdAndUserTypeAndUserId(
            communityId,
            USER_TYPE_USER,
            userId
        );
        if (entity == null) {
            throw new ResourceNotFoundException("member");
        }
        return entity;
    }

    private List<MemberModel> toModels(List<UserGroupMember> entities) {
        return entities
            .stream()
            .map(entityToModelConverter::toModel)
            .collect(Collectors.toList());
    }
}
