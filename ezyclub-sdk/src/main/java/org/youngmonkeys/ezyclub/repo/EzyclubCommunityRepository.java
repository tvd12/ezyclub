package org.youngmonkeys.ezyclub.repo;

import com.tvd12.ezydata.database.EzyDatabaseRepository;
import com.tvd12.ezyfox.database.annotation.EzyQuery;
import com.tvd12.ezyfox.database.annotation.EzyRepository;
import com.tvd12.ezyfox.util.Next;
import org.youngmonkeys.ezycrm.entity.UserGroup;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@EzyRepository
public interface EzyclubCommunityRepository
    extends EzyDatabaseRepository<Long, UserGroup> {

    UserGroup findByGroupTypeAndGroupCode(
        String groupType,
        String groupCode
    );

    List<UserGroup> findByIdIn(Collection<Long> ids);

    @EzyQuery(
        "SELECT e FROM UserGroup e " +
            "WHERE e.groupType = ?0 " +
            "AND e.status = ?1 " +
            "ORDER BY e.priority DESC, e.id DESC"
    )
    List<UserGroup> findByGroupTypeAndStatusOrderByPriorityDescIdDesc(
        String groupType,
        String status,
        Next next
    );

    @EzyQuery(
        "SELECT e FROM UserGroup e " +
            "WHERE e.groupType = ?0 " +
            "AND e.status = ?1 " +
            "AND e.groupName LIKE CONCAT('%', ?2, '%') " +
            "ORDER BY e.priority DESC, e.id DESC"
    )
    List<UserGroup> findByGroupTypeAndStatusAndKeywordOrderByPriorityDescIdDesc(
        String groupType,
        String status,
        String keyword,
        Next next
    );

    @EzyQuery(
        "SELECT e FROM UserGroup e " +
            "INNER JOIN DataMeta m ON m.dataId = e.id " +
            "WHERE m.dataType = ?2 " +
            "AND m.metaKey = ?3 " +
            "AND m.metaValue = ?4 " +
            "AND e.groupType = ?0 " +
            "AND e.status = ?1 " +
            "ORDER BY e.priority DESC, e.id DESC"
    )
    List<UserGroup> findByGroupTypeAndStatusAndMetaOrderByPriorityDescIdDesc(
        String groupType,
        String status,
        String dataType,
        String metaKey,
        String metaValue,
        Next next
    );

    @EzyQuery(
        "SELECT COUNT(e) FROM UserGroup e " +
            "WHERE e.groupType = ?0 " +
            "AND e.status = ?1"
    )
    long countByGroupTypeAndStatus(
        String groupType,
        String status
    );

    @EzyQuery(
        "SELECT e FROM UserGroup e " +
            "WHERE e.groupType = ?0 " +
            "ORDER BY e.id DESC"
    )
    List<UserGroup> findByGroupTypeOrderByIdDesc(
        String groupType,
        Next next
    );

    @EzyQuery(
        "SELECT COUNT(e) FROM UserGroup e " +
            "WHERE e.groupType = ?0"
    )
    long countByGroupType(String groupType);

    @EzyQuery(
        "UPDATE UserGroup e " +
            "SET e.priority = ?1, e.updatedAt = ?2 " +
            "WHERE e.id = ?0"
    )
    void updatePriorityById(
        long id,
        int priority,
        LocalDateTime updatedAt
    );
}
