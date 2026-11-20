package org.youngmonkeys.ezyclub.service;

import com.tvd12.ezyfox.bean.annotation.EzyAutoBind;
import com.tvd12.ezyfox.util.Next;
import lombok.Setter;
import org.youngmonkeys.ezyarticle.sdk.entity.Comment;
import org.youngmonkeys.ezyarticle.sdk.entity.CommentStatus;
import org.youngmonkeys.ezyarticle.sdk.entity.CommentType;
import org.youngmonkeys.ezyarticle.sdk.entity.Post;
import org.youngmonkeys.ezyarticle.sdk.entity.PostCommentStatus;
import org.youngmonkeys.ezyarticle.sdk.entity.PostMeta;
import org.youngmonkeys.ezyarticle.sdk.entity.PostStatus;
import org.youngmonkeys.ezyarticle.sdk.repo.PostMetaRepository;
import org.youngmonkeys.ezyarticle.sdk.repo.PostRepository;
import org.youngmonkeys.ezyclub.converter.EzyclubEntityToModelConverter;
import org.youngmonkeys.ezyclub.model.CommentModel;
import org.youngmonkeys.ezyclub.model.EzyclubPageModel;
import org.youngmonkeys.ezyclub.model.FeedPostModel;
import org.youngmonkeys.ezyclub.model.SaveFeedPostModel;
import org.youngmonkeys.ezyclub.repo.EzyclubCommentRepository;
import org.youngmonkeys.ezyclub.repo.EzyclubPostRepository;
import org.youngmonkeys.ezyplatform.exception.ResourceNotFoundException;
import org.youngmonkeys.ezyplatform.time.ClockProxy;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.META_KEY_CATEGORY;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.POST_TYPE_FEED_POST;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.PRIORITY_NORMAL;
import static org.youngmonkeys.ezyclub.constant.EzyclubConstants.PRIORITY_PINNED;

@Setter
public class EzyclubFeedPostService {

    @EzyAutoBind
    protected EzyclubPostRepository ezyclubPostRepository;

    @EzyAutoBind
    protected PostRepository postRepository;

    @EzyAutoBind
    protected PostMetaRepository postMetaRepository;

    @EzyAutoBind
    protected EzyclubCommentRepository commentRepository;

    @EzyAutoBind
    protected EzyclubEntityToModelConverter entityToModelConverter;

    @EzyAutoBind
    protected ClockProxy clock;

    public long addPost(long communityId, long authorUserId, SaveFeedPostModel model) {
        LocalDateTime now = clock.nowDateTime();
        Post entity = new Post();
        entity.setPostType(POST_TYPE_FEED_POST);
        entity.setParentId(communityId);
        entity.setAuthorUserId(authorUserId);
        entity.setTitle(model.getTitle());
        entity.setContent(model.getContent());
        entity.setContentType("TEXT");
        entity.setStatus(PostStatus.PUBLISHED.toString());
        entity.setCommentStatus(PostCommentStatus.OPEN.toString());
        entity.setPriority(PRIORITY_NORMAL);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setPublishedAt(now);
        ezyclubPostRepository.save(entity);
        savePostMeta(entity.getId(), META_KEY_CATEGORY, model.getCategory());
        return entity.getId();
    }

    public void updatePost(long postId, SaveFeedPostModel model) {
        Post entity = getFeedPostEntityOrThrow(postId);
        entity.setTitle(model.getTitle());
        entity.setContent(model.getContent());
        entity.setUpdatedAt(clock.nowDateTime());
        ezyclubPostRepository.save(entity);
        savePostMeta(postId, META_KEY_CATEGORY, model.getCategory());
    }

    public void removePost(long postId) {
        Post entity = getFeedPostEntityOrThrow(postId);
        entity.setStatus(PostStatus.TRASH.toString());
        entity.setUpdatedAt(clock.nowDateTime());
        ezyclubPostRepository.save(entity);
    }

    public void updatePinned(long postId, boolean pinned) {
        Post entity = getFeedPostEntityOrThrow(postId);
        entity.setPriority(pinned ? PRIORITY_PINNED : PRIORITY_NORMAL);
        entity.setUpdatedAt(clock.nowDateTime());
        ezyclubPostRepository.save(entity);
    }

    public FeedPostModel getPublishedPostById(long postId) {
        Post entity = ezyclubPostRepository.findById(postId);
        if (entity == null
            || !POST_TYPE_FEED_POST.equals(entity.getPostType())
            || !PostStatus.PUBLISHED.toString().equals(entity.getStatus())
        ) {
            return null;
        }
        return entityToModelConverter.toFeedPostModel(
            entity,
            getPostMetaValue(postId, META_KEY_CATEGORY)
        );
    }

    public FeedPostModel getPublishedPostByIdOrThrow(long postId) {
        FeedPostModel post = getPublishedPostById(postId);
        if (post == null) {
            throw new ResourceNotFoundException("post");
        }
        return post;
    }

    public EzyclubPageModel<FeedPostModel> getPublishedPostPage(
        long communityId,
        String category,
        int page,
        int limit
    ) {
        Next next = Next.fromSkipLimit((long) page * limit, limit + 1);
        String status = PostStatus.PUBLISHED.toString();
        List<Post> entities = category == null
            ? ezyclubPostRepository.findByParentIdAndStatusAndPostTypeOrderByPriorityDesc(
                communityId,
                status,
                POST_TYPE_FEED_POST,
                next
            )
            : ezyclubPostRepository.findByParentIdAndStatusAndPostTypeAndMetaOrderByPriorityDesc(
                communityId,
                status,
                POST_TYPE_FEED_POST,
                META_KEY_CATEGORY,
                category,
                next
            );
        Map<Long, String> categoryByPostId = getCategoryMap(entities);
        return EzyclubPageModel.of(
            entities
                .stream()
                .map(it -> entityToModelConverter.toFeedPostModel(
                    it,
                    categoryByPostId.get(it.getId())
                ))
                .collect(Collectors.toList()),
            page,
            limit
        );
    }

    public long countPublishedPosts(long communityId) {
        return ezyclubPostRepository.countByParentIdAndStatusAndPostType(
            communityId,
            PostStatus.PUBLISHED.toString(),
            POST_TYPE_FEED_POST
        );
    }

    public long addComment(
        long postId,
        long authorUserId,
        long parentCommentId,
        String content
    ) {
        LocalDateTime now = clock.nowDateTime();
        Comment entity = new Comment();
        entity.setPostId(postId);
        entity.setAuthorUserId(authorUserId);
        entity.setParentId(parentCommentId);
        entity.setContent(content);
        entity.setContentType("TEXT");
        entity.setCommentType(CommentType.COMMENT.toString());
        entity.setStatus(CommentStatus.ACCEPTED.toString());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        commentRepository.save(entity);
        postRepository.updateToIncreasePostCommentCountById(postId);
        return entity.getId();
    }

    public void removeComment(long commentId) {
        Comment entity = getCommentEntityOrThrow(commentId);
        if (!CommentStatus.ACCEPTED.toString().equals(entity.getStatus())) {
            return;
        }
        entity.setStatus(CommentStatus.TRASH.toString());
        entity.setUpdatedAt(clock.nowDateTime());
        commentRepository.save(entity);
        postRepository.updateToDecreasePostCommentCountById(entity.getPostId());
    }

    public CommentModel getCommentById(long commentId) {
        Comment entity = commentRepository.findById(commentId);
        if (entity == null
            || !CommentStatus.ACCEPTED.toString().equals(entity.getStatus())
        ) {
            return null;
        }
        return entityToModelConverter.toModel(entity);
    }

    public List<CommentModel> getAcceptedComments(long postId, int limit) {
        return commentRepository
            .findByPostIdAndCommentTypeAndStatusOrderByIdAsc(
                postId,
                CommentType.COMMENT.toString(),
                CommentStatus.ACCEPTED.toString(),
                Next.limit(limit)
            )
            .stream()
            .map(entityToModelConverter::toModel)
            .collect(Collectors.toList());
    }

    protected Post getFeedPostEntityOrThrow(long postId) {
        Post entity = ezyclubPostRepository.findById(postId);
        if (entity == null || !POST_TYPE_FEED_POST.equals(entity.getPostType())) {
            throw new ResourceNotFoundException("post");
        }
        return entity;
    }

    protected Comment getCommentEntityOrThrow(long commentId) {
        Comment entity = commentRepository.findById(commentId);
        if (entity == null) {
            throw new ResourceNotFoundException("comment");
        }
        return entity;
    }

    protected void savePostMeta(long postId, String metaKey, String metaValue) {
        PostMeta entity = postMetaRepository
            .findByPostIdAndMetaKey(postId, metaKey)
            .orElseGet(PostMeta::new);
        entity.setPostId(postId);
        entity.setMetaKey(metaKey);
        entity.setMetaValue(metaValue == null ? "" : metaValue);
        if (entity.getMetaNumberValue() == null) {
            entity.setMetaNumberValue(BigInteger.ZERO);
        }
        postMetaRepository.save(entity);
    }

    protected String getPostMetaValue(long postId, String metaKey) {
        return postMetaRepository
            .findByPostIdAndMetaKey(postId, metaKey)
            .map(PostMeta::getMetaValue)
            .orElse("");
    }

    private Map<Long, String> getCategoryMap(List<Post> entities) {
        if (entities.isEmpty()) {
            return Collections.emptyMap();
        }
        return postMetaRepository
            .findByPostIdInAndMetaKey(
                entities.stream().map(Post::getId).collect(Collectors.toList()),
                META_KEY_CATEGORY
            )
            .stream()
            .collect(
                Collectors.toMap(
                    PostMeta::getPostId,
                    it -> it.getMetaValue() == null ? "" : it.getMetaValue(),
                    (o, n) -> n
                )
            );
    }
}
