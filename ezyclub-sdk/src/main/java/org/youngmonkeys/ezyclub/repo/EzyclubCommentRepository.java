package org.youngmonkeys.ezyclub.repo;

import com.tvd12.ezydata.database.EzyDatabaseRepository;
import com.tvd12.ezyfox.database.annotation.EzyQuery;
import com.tvd12.ezyfox.database.annotation.EzyRepository;
import com.tvd12.ezyfox.util.Next;
import org.youngmonkeys.ezyarticle.sdk.entity.Comment;

import java.util.List;

@EzyRepository
public interface EzyclubCommentRepository
    extends EzyDatabaseRepository<Long, Comment> {

    @EzyQuery(
        "SELECT e FROM Comment e " +
            "WHERE e.postId = ?0 " +
            "AND e.commentType = ?1 " +
            "AND e.status = ?2 " +
            "ORDER BY e.id ASC"
    )
    List<Comment> findByPostIdAndCommentTypeAndStatusOrderByIdAsc(
        long postId,
        String commentType,
        String status,
        Next next
    );
}
