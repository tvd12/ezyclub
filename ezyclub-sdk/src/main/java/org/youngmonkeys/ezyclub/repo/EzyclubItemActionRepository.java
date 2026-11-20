package org.youngmonkeys.ezyclub.repo;

import com.tvd12.ezydata.database.EzyDatabaseRepository;
import com.tvd12.ezyfox.database.annotation.EzyQuery;
import com.tvd12.ezyfox.database.annotation.EzyRepository;
import org.youngmonkeys.ezyrating.entity.ItemAction;

import java.util.Collection;
import java.util.List;

@EzyRepository
public interface EzyclubItemActionRepository
    extends EzyDatabaseRepository<Long, ItemAction> {

    ItemAction findByItemTypeAndItemNameAndItemIdAndActionType(
        String itemType,
        String itemName,
        long itemId,
        String actionType
    );

    @EzyQuery(
        "SELECT e FROM ItemAction e " +
            "WHERE e.itemType = ?0 " +
            "AND e.itemName = ?1 " +
            "AND e.itemId IN ?2 " +
            "AND e.actionType = ?3"
    )
    List<ItemAction> findByItemTypeAndItemNameAndItemIdInAndActionType(
        String itemType,
        String itemName,
        Collection<Long> itemIds,
        String actionType
    );
}
