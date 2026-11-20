package org.youngmonkeys.ezyclub.repo;

import com.tvd12.ezydata.database.EzyDatabaseRepository;
import com.tvd12.ezyfox.database.annotation.EzyQuery;
import com.tvd12.ezyfox.database.annotation.EzyRepository;
import com.tvd12.ezyfox.util.Next;
import org.youngmonkeys.ezycrm.entity.UserGroupMember;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@EzyRepository
public interface EzyclubMemberRepository
    extends EzyDatabaseRepository<Long, UserGroupMember> {

    UserGroupMember findByUserGroupIdAndUserTypeAndUserId(
        long userGroupId,
        String userType,
        long userId
    );

    List<UserGroupMember> findByUserGroupIdAndUserTypeAndUserIdIn(
        long userGroupId,
        String userType,
        Collection<Long> userIds
    );

    @EzyQuery(
        "SELECT e FROM UserGroupMember e " +
            "WHERE e.userGroupId = ?0 " +
            "AND e.userType = ?1 " +
            "AND e.status = ?2 " +
            "ORDER BY e.scores DESC, e.id ASC"
    )
    List<UserGroupMember> findByUserGroupIdAndUserTypeAndStatusOrderByScoresDesc(
        long userGroupId,
        String userType,
        String status,
        Next next
    );

    @EzyQuery(
        "SELECT e FROM UserGroupMember e " +
            "WHERE e.userGroupId = ?0 " +
            "AND e.userType = ?1 " +
            "AND e.status = ?2 " +
            "ORDER BY e.id DESC"
    )
    List<UserGroupMember> findByUserGroupIdAndUserTypeAndStatusOrderByIdDesc(
        long userGroupId,
        String userType,
        String status,
        Next next
    );

    @EzyQuery(
        "SELECT e FROM UserGroupMember e " +
            "WHERE e.userType = ?0 " +
            "AND e.userId = ?1 " +
            "AND e.status = ?2 " +
            "ORDER BY e.id DESC"
    )
    List<UserGroupMember> findByUserTypeAndUserIdAndStatusOrderByIdDesc(
        String userType,
        long userId,
        String status,
        Next next
    );

    @EzyQuery(
        "SELECT COUNT(e) FROM UserGroupMember e " +
            "WHERE e.userGroupId = ?0 " +
            "AND e.userType = ?1 " +
            "AND e.status = ?2"
    )
    long countByUserGroupIdAndUserTypeAndStatus(
        long userGroupId,
        String userType,
        String status
    );

    @EzyQuery(
        "SELECT COUNT(e) FROM UserGroupMember e " +
            "WHERE e.userGroupId = ?0 " +
            "AND e.userType = ?1 " +
            "AND e.status = ?2 " +
            "AND e.scores > ?3"
    )
    long countByUserGroupIdAndUserTypeAndStatusAndScoresGt(
        long userGroupId,
        String userType,
        String status,
        BigDecimal scores
    );

    @EzyQuery(
        "UPDATE UserGroupMember e " +
            "SET e.scores = e.scores + ?3, e.updatedAt = ?4 " +
            "WHERE e.userGroupId = ?0 " +
            "AND e.userType = ?1 " +
            "AND e.userId = ?2"
    )
    void updateToIncreaseScores(
        long userGroupId,
        String userType,
        long userId,
        BigDecimal value,
        LocalDateTime updatedAt
    );
}
