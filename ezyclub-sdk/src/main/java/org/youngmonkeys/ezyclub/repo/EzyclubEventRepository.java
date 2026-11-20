package org.youngmonkeys.ezyclub.repo;

import com.tvd12.ezydata.database.EzyDatabaseRepository;
import com.tvd12.ezyfox.database.annotation.EzyQuery;
import com.tvd12.ezyfox.database.annotation.EzyRepository;
import com.tvd12.ezyfox.util.Next;
import org.youngmonkeys.ezycrm.entity.EzyCRMEvent;

import java.time.LocalDateTime;
import java.util.List;

@EzyRepository
public interface EzyclubEventRepository
    extends EzyDatabaseRepository<Long, EzyCRMEvent> {

    @EzyQuery(
        "SELECT e FROM EzyCRMEvent e " +
            "WHERE e.parentId = ?0 " +
            "AND e.eventType = ?1 " +
            "AND e.status = ?2 " +
            "AND e.startAt >= ?3 " +
            "AND e.startAt < ?4 " +
            "ORDER BY e.startAt ASC, e.id ASC"
    )
    List<EzyCRMEvent> findByParentIdAndEventTypeAndStatusAndStartAtBetween(
        long parentId,
        String eventType,
        String status,
        LocalDateTime fromTime,
        LocalDateTime toTime,
        Next next
    );
}
