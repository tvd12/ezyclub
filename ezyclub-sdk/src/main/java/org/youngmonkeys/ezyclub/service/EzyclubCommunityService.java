package org.youngmonkeys.ezyclub.service;

import com.tvd12.ezyfox.bean.annotation.EzyAutoBind;
import com.tvd12.ezyfox.util.Next;
import lombok.Setter;
import org.youngmonkeys.ezyclub.constant.MemberRole;
import org.youngmonkeys.ezyclub.converter.EzyclubEntityToModelConverter;
import org.youngmonkeys.ezyclub.model.CommunityModel;
import org.youngmonkeys.ezyclub.model.EzyclubPageModel;
import org.youngmonkeys.ezyclub.model.SaveCommunityModel;
import org.youngmonkeys.ezyclub.repo.EzyclubCommunityRepository;
import org.youngmonkeys.ezyclub.repo.EzyclubMemberRepository;
import org.youngmonkeys.ezycrm.entity.UserGroup;
import org.youngmonkeys.ezycrm.entity.UserGroupMember;
import org.youngmonkeys.ezycrm.entity.UserGroupMemberStatus;
import org.youngmonkeys.ezycrm.entity.UserGroupStatus;
import org.youngmonkeys.ezyplatform.exception.BadRequestException;
import org.youngmonkeys.ezyplatform.exception.ResourceNotFoundException;
import org.youngmonkeys.ezyplatform.service.DataMetaService;
import org.youngmonkeys.ezyplatform.time.ClockProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.DATA_TYPE_COMMUNITY;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.GROUP_TYPE_COMMUNITY;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_CATEGORY;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_DESCRIPTION;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_POST_CATEGORIES;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_PRIVACY;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_SHORT_DESCRIPTION;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_THEME_COLOR;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.USER_TYPE_USER;

@Setter
public class EzyclubCommunityService {

    @EzyAutoBind
    protected EzyclubCommunityRepository communityRepository;

    @EzyAutoBind
    protected EzyclubMemberRepository memberRepository;

    @EzyAutoBind
    protected DataMetaService dataMetaService;

    @EzyAutoBind
    protected EzyclubEntityToModelConverter entityToModelConverter;

    @EzyAutoBind
    protected ClockProxy clock;

    public static final LocalDateTime MEMBER_NEVER_EXPIRED_AT =
        LocalDateTime.of(2999, 12, 31, 0, 0);

    public long addCommunity(long ownerUserId, SaveCommunityModel model) {
        if (containsCommunityBySlug(model.getSlug())) {
            throw new BadRequestException(
                Collections.singletonMap("slug", "duplicated")
            );
        }
        LocalDateTime now = clock.nowDateTime();
        UserGroup entity = new UserGroup();
        entity.setGroupType(GROUP_TYPE_COMMUNITY);
        entity.setGroupCode(model.getSlug());
        entity.setGroupName(model.getName());
        entity.setBannerId(model.getBannerImageId());
        entity.setLogoId(model.getLogoImageId());
        entity.setStatus(UserGroupStatus.ACTIVATED.toString());
        entity.setPriority(1);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        communityRepository.save(entity);
        saveCommunityMetas(entity.getId(), model);
        UserGroupMember owner = new UserGroupMember();
        owner.setUserGroupId(entity.getId());
        owner.setUserType(USER_TYPE_USER);
        owner.setUserId(ownerUserId);
        owner.setMemberType(MemberRole.OWNER.toString());
        owner.setScores(BigDecimal.ZERO);
        owner.setStatus(UserGroupMemberStatus.ACTIVATED.toString());
        owner.setExpiredAt(MEMBER_NEVER_EXPIRED_AT);
        owner.setCreatedAt(now);
        owner.setUpdatedAt(now);
        memberRepository.save(owner);
        return entity.getId();
    }

    public void updateCommunity(long communityId, SaveCommunityModel model) {
        UserGroup entity = getCommunityEntityOrThrow(communityId);
        entity.setGroupName(model.getName());
        entity.setBannerId(model.getBannerImageId());
        entity.setLogoId(model.getLogoImageId());
        entity.setUpdatedAt(clock.nowDateTime());
        communityRepository.save(entity);
        saveCommunityMetas(communityId, model);
    }

    public void updateCommunityStatus(long communityId, String status) {
        UserGroup entity = getCommunityEntityOrThrow(communityId);
        entity.setStatus(status);
        entity.setUpdatedAt(clock.nowDateTime());
        communityRepository.save(entity);
    }

    public void refreshMemberCount(long communityId) {
        long memberCount = memberRepository.countByUserGroupIdAndUserTypeAndStatus(
            communityId,
            USER_TYPE_USER,
            UserGroupMemberStatus.ACTIVATED.toString()
        );
        communityRepository.updatePriorityById(
            communityId,
            (int) Math.min(Integer.MAX_VALUE, memberCount),
            clock.nowDateTime()
        );
    }

    public boolean containsCommunityBySlug(String slug) {
        return communityRepository.findByGroupTypeAndGroupCode(
            GROUP_TYPE_COMMUNITY,
            slug
        ) != null;
    }

    public CommunityModel getCommunityById(long communityId) {
        UserGroup entity = communityRepository.findById(communityId);
        if (entity == null || !GROUP_TYPE_COMMUNITY.equals(entity.getGroupType())) {
            return null;
        }
        return toModel(entity);
    }

    public CommunityModel getCommunityBySlug(String slug) {
        return toModel(
            communityRepository.findByGroupTypeAndGroupCode(
                GROUP_TYPE_COMMUNITY,
                slug
            )
        );
    }

    public CommunityModel getActivatedCommunityBySlugOrThrow(String slug) {
        CommunityModel community = getCommunityBySlug(slug);
        if (community == null
            || !UserGroupStatus.ACTIVATED.toString().equals(community.getStatus())
        ) {
            throw new ResourceNotFoundException("community");
        }
        return community;
    }

    public CommunityModel getActivatedCommunityByIdOrThrow(long communityId) {
        CommunityModel community = getCommunityById(communityId);
        if (community == null
            || !UserGroupStatus.ACTIVATED.toString().equals(community.getStatus())
        ) {
            throw new ResourceNotFoundException("community");
        }
        return community;
    }

    public List<CommunityModel> getCommunitiesByIds(Collection<Long> communityIds) {
        if (communityIds.isEmpty()) {
            return Collections.emptyList();
        }
        return communityRepository.findByIdIn(communityIds)
            .stream()
            .filter(it -> GROUP_TYPE_COMMUNITY.equals(it.getGroupType()))
            .map(this::toModel)
            .collect(Collectors.toList());
    }

    public EzyclubPageModel<CommunityModel> getActivatedCommunityPage(
        String keyword,
        String category,
        int page,
        int limit
    ) {
        Next next = Next.fromSkipLimit((long) page * limit, limit + 1);
        String status = UserGroupStatus.ACTIVATED.toString();
        List<UserGroup> entities;
        if (category != null) {
            entities = communityRepository
                .findByGroupTypeAndStatusAndMetaOrderByPriorityDescIdDesc(
                    GROUP_TYPE_COMMUNITY,
                    status,
                    DATA_TYPE_COMMUNITY,
                    META_KEY_CATEGORY,
                    category,
                    next
                );
        } else if (keyword != null) {
            entities = communityRepository
                .findByGroupTypeAndStatusAndKeywordOrderByPriorityDescIdDesc(
                    GROUP_TYPE_COMMUNITY,
                    status,
                    keyword,
                    next
                );
        } else {
            entities = communityRepository
                .findByGroupTypeAndStatusOrderByPriorityDescIdDesc(
                    GROUP_TYPE_COMMUNITY,
                    status,
                    next
                );
        }
        return EzyclubPageModel.of(
            entities.stream().map(this::toModel).collect(Collectors.toList()),
            page,
            limit
        );
    }

    public EzyclubPageModel<CommunityModel> getCommunityPage(int page, int limit) {
        List<UserGroup> entities = communityRepository.findByGroupTypeOrderByIdDesc(
            GROUP_TYPE_COMMUNITY,
            Next.fromSkipLimit((long) page * limit, limit + 1)
        );
        return EzyclubPageModel.of(
            entities.stream().map(this::toModel).collect(Collectors.toList()),
            page,
            limit
        );
    }

    public long countActivatedCommunities() {
        return communityRepository.countByGroupTypeAndStatus(
            GROUP_TYPE_COMMUNITY,
            UserGroupStatus.ACTIVATED.toString()
        );
    }

    public long countCommunities() {
        return communityRepository.countByGroupType(GROUP_TYPE_COMMUNITY);
    }

    protected UserGroup getCommunityEntityOrThrow(long communityId) {
        UserGroup entity = communityRepository.findById(communityId);
        if (entity == null || !GROUP_TYPE_COMMUNITY.equals(entity.getGroupType())) {
            throw new ResourceNotFoundException("community");
        }
        return entity;
    }

    protected void saveCommunityMetas(long communityId, SaveCommunityModel model) {
        dataMetaService.saveDataMetaUniqueKey(
            DATA_TYPE_COMMUNITY,
            communityId,
            META_KEY_CATEGORY,
            model.getCategory()
        );
        dataMetaService.saveDataMetaUniqueKey(
            DATA_TYPE_COMMUNITY,
            communityId,
            META_KEY_PRIVACY,
            model.getPrivacy()
        );
        dataMetaService.saveDataMetaUniqueKey(
            DATA_TYPE_COMMUNITY,
            communityId,
            META_KEY_SHORT_DESCRIPTION,
            model.getShortDescription()
        );
        dataMetaService.saveDataMetaUniqueKey(
            DATA_TYPE_COMMUNITY,
            communityId,
            META_KEY_THEME_COLOR,
            model.getThemeColor()
        );
        dataMetaService.saveDataMetaUniqueKey(
            DATA_TYPE_COMMUNITY,
            communityId,
            META_KEY_POST_CATEGORIES,
            String.join(",", model.getPostCategories())
        );
        dataMetaService.saveDataMetaUniqueKey(
            DATA_TYPE_COMMUNITY,
            communityId,
            META_KEY_DESCRIPTION,
            "",
            model.getDescription()
        );
    }

    protected CommunityModel toModel(UserGroup entity) {
        if (entity == null) {
            return null;
        }
        return entityToModelConverter.toModel(
            entity,
            dataMetaService.getDataMetaValues(DATA_TYPE_COMMUNITY, entity.getId()),
            dataMetaService.getDataMetaTextValues(DATA_TYPE_COMMUNITY, entity.getId())
        );
    }
}
