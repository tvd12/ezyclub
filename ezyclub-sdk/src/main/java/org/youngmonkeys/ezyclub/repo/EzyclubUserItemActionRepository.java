package org.youngmonkeys.ezyclub.repo;

import com.tvd12.ezydata.database.EzyDatabaseRepository;
import com.tvd12.ezyfox.database.annotation.EzyQuery;
import com.tvd12.ezyfox.database.annotation.EzyRepository;
import org.youngmonkeys.ezyrating.entity.UserItemAction;

import java.util.Collection;
import java.util.List;

@EzyRepository
public interface EzyclubUserItemActionRepository
    extends EzyDatabaseRepository<Long, UserItemAction> {

    UserItemAction findByItemTypeAndItemNameAndItemIdAndActionTypeAndFromUserIdAndFromIp(
        String itemType,
        String itemName,
        long itemId,
        String actionType,
        long fromUserId,
        String fromIp
    );

    @EzyQuery(
        "SELECT e FROM UserItemAction e " +
            "WHERE e.itemType = ?0 " +
            "AND e.itemName = ?1 " +
            "AND e.itemId IN ?2 " +
            "AND e.actionType = ?3 " +
            "AND e.fromUserId = ?4 " +
            "AND e.fromIp = ?5 " +
            "AND e.totalActionCount > 0"
    )
    List<UserItemAction> findActiveByItemIdsAndUser(
        String itemType,
        String itemName,
        Collection<Long> itemIds,
        String actionType,
        long fromUserId,
        String fromIp
    );
}
