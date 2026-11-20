package org.youngmonkeys.ezyclub.service;

import com.tvd12.ezyfox.bean.annotation.EzyAutoBind;
import lombok.Setter;
import org.youngmonkeys.ezyclub.model.LikeResultModel;
import org.youngmonkeys.ezyclub.repo.EzyclubItemActionRepository;
import org.youngmonkeys.ezyclub.repo.EzyclubUserItemActionRepository;
import org.youngmonkeys.ezyplatform.time.ClockProxy;
import org.youngmonkeys.ezyrating.entity.ItemAction;
import org.youngmonkeys.ezyrating.entity.UserItemAction;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.ACTION_TYPE_LIKE;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.FROM_IP_ANY;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.ITEM_NAME_EZYCLUB;

@Setter
public class EzyclubLikeService {

    @EzyAutoBind
    protected EzyclubItemActionRepository itemActionRepository;

    @EzyAutoBind
    protected EzyclubUserItemActionRepository userItemActionRepository;

    @EzyAutoBind
    protected ClockProxy clock;

    public LikeResultModel toggleLike(String itemType, long itemId, long userId) {
        LocalDateTime now = clock.nowDateTime();
        UserItemAction userAction = userItemActionRepository
            .findByItemTypeAndItemNameAndItemIdAndActionTypeAndFromUserIdAndFromIp(
                itemType,
                ITEM_NAME_EZYCLUB,
                itemId,
                ACTION_TYPE_LIKE,
                userId,
                FROM_IP_ANY
            );
        if (userAction == null) {
            userAction = new UserItemAction();
            userAction.setItemType(itemType);
            userAction.setItemName(ITEM_NAME_EZYCLUB);
            userAction.setItemId(itemId);
            userAction.setActionType(ACTION_TYPE_LIKE);
            userAction.setFromUserId(userId);
            userAction.setFromIp(FROM_IP_ANY);
            userAction.setCreatedAt(now);
        }
        boolean liked = userAction.getTotalActionCount() <= 0;
        userAction.setTotalActionCount(liked ? 1 : 0);
        userAction.setUpdatedAt(now);
        userItemActionRepository.save(userAction);
        ItemAction itemAction = itemActionRepository
            .findByItemTypeAndItemNameAndItemIdAndActionType(
                itemType,
                ITEM_NAME_EZYCLUB,
                itemId,
                ACTION_TYPE_LIKE
            );
        if (itemAction == null) {
            itemAction = new ItemAction();
            itemAction.setItemType(itemType);
            itemAction.setItemName(ITEM_NAME_EZYCLUB);
            itemAction.setItemId(itemId);
            itemAction.setActionType(ACTION_TYPE_LIKE);
            itemAction.setCreatedAt(now);
        }
        itemAction.setTotalActionCount(
            Math.max(0, itemAction.getTotalActionCount() + (liked ? 1 : -1))
        );
        itemAction.setUpdatedAt(now);
        itemActionRepository.save(itemAction);
        return LikeResultModel.builder()
            .liked(liked)
            .likeCount(itemAction.getTotalActionCount())
            .build();
    }

    public Map<Long, Long> getLikeCountMap(String itemType, Collection<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return itemActionRepository
            .findByItemTypeAndItemNameAndItemIdInAndActionType(
                itemType,
                ITEM_NAME_EZYCLUB,
                itemIds,
                ACTION_TYPE_LIKE
            )
            .stream()
            .collect(
                Collectors.toMap(
                    ItemAction::getItemId,
                    ItemAction::getTotalActionCount,
                    (o, n) -> n
                )
            );
    }

    public Set<Long> getLikedItemIds(
        String itemType,
        Collection<Long> itemIds,
        long userId
    ) {
        if (itemIds.isEmpty() || userId <= 0) {
            return Collections.emptySet();
        }
        return userItemActionRepository
            .findActiveByItemIdsAndUser(
                itemType,
                ITEM_NAME_EZYCLUB,
                itemIds,
                ACTION_TYPE_LIKE,
                userId,
                FROM_IP_ANY
            )
            .stream()
            .map(UserItemAction::getItemId)
            .collect(Collectors.toSet());
    }
}
