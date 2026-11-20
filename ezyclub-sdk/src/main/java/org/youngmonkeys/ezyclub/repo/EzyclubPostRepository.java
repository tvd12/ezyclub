package org.youngmonkeys.ezyclub.repo;

import com.tvd12.ezydata.database.EzyDatabaseRepository;
import com.tvd12.ezyfox.database.annotation.EzyQuery;
import com.tvd12.ezyfox.database.annotation.EzyRepository;
import com.tvd12.ezyfox.util.Next;
import org.youngmonkeys.ezyarticle.sdk.entity.Post;

import java.util.List;

@EzyRepository
public interface EzyclubPostRepository
    extends EzyDatabaseRepository<Long, Post> {

    @EzyQuery(
        "SELECT e FROM Post e " +
            "WHERE e.parentId = ?0 " +
            "AND e.status = ?1 " +
            "AND e.postType = ?2 " +
            "ORDER BY e.priority DESC, e.publishedAt DESC, e.id DESC"
    )
    List<Post> findByParentIdAndStatusAndPostTypeOrderByPriorityDesc(
        long parentId,
        String status,
        String postType,
        Next next
    );

    @EzyQuery(
        "SELECT e FROM Post e " +
            "INNER JOIN PostMeta m ON m.postId = e.id " +
            "WHERE e.parentId = ?0 " +
            "AND e.status = ?1 " +
            "AND e.postType = ?2 " +
            "AND m.metaKey = ?3 " +
            "AND m.metaValue = ?4 " +
            "ORDER BY e.priority DESC, e.publishedAt DESC, e.id DESC"
    )
    List<Post> findByParentIdAndStatusAndPostTypeAndMetaOrderByPriorityDesc(
        long parentId,
        String status,
        String postType,
        String metaKey,
        String metaValue,
        Next next
    );

    @EzyQuery(
        "SELECT e FROM Post e " +
            "WHERE e.parentId = ?0 " +
            "AND e.status = ?1 " +
            "AND e.postType = ?2 " +
            "ORDER BY e.priority ASC, e.id ASC"
    )
    List<Post> findByParentIdAndStatusAndPostTypeOrderByPriorityAsc(
        long parentId,
        String status,
        String postType
    );

    @EzyQuery(
        "SELECT COUNT(e) FROM Post e " +
            "WHERE e.parentId = ?0 " +
            "AND e.status = ?1 " +
            "AND e.postType = ?2"
    )
    long countByParentIdAndStatusAndPostType(
        long parentId,
        String status,
        String postType
    );

    @EzyQuery(
        "SELECT e FROM Post e " +
            "WHERE e.parentId = ?0 " +
            "AND e.postType = ?1 " +
            "ORDER BY e.id DESC"
    )
    List<Post> findByParentIdAndPostTypeOrderByIdDesc(
        long parentId,
        String postType,
        Next next
    );
}
