package park.bumsiku.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import park.bumsiku.domain.dto.request.CommentRequest;
import park.bumsiku.domain.dto.response.*;
import park.bumsiku.domain.entity.Category;
import park.bumsiku.domain.entity.Comment;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.domain.entity.Tag;
import park.bumsiku.repository.CategoryRepository;
import park.bumsiku.repository.CommentRepository;
import park.bumsiku.repository.PostRepository;
import park.bumsiku.repository.TagRepository;
import park.bumsiku.utils.integration.DiscordWebhookCreator;
import park.bumsiku.utils.monitoring.LogExecutionTime;
import park.bumsiku.utils.sorting.PostSortBuilder;
import park.bumsiku.utils.sorting.SortCriteria;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class PublicService {

    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private CategoryRepository categoryRepository;
    private TagRepository tagRepository;
    private PostSortBuilder postSortBuilder;

    private DiscordWebhookCreator discord;

    @LogExecutionTime
    public PostListResponse getPostList(int page, int size, String sort) {
        SortCriteria sortCriteria = postSortBuilder.buildSortCriteria(sort);
        List<Post> posts = postRepository.findAll(page, size, sortCriteria.jpqlOrderClause());
        int totalElements = postRepository.countAll();

        return buildPostListResponse(posts, totalElements, page, size);
    }

    @LogExecutionTime
    public PostListResponse getPostList(int categoryId, int page, int size, String sort) {
        SortCriteria sortCriteria = postSortBuilder.buildSortCriteria(sort);
        List<Post> posts = postRepository.findAllByCategoryId(categoryId, page, size, sortCriteria.jpqlOrderClause());
        int totalElements = postRepository.countByCategoryId(categoryId);

        return buildPostListResponse(posts, totalElements, page, size);
    }

    @LogExecutionTime
    public PostResponse getPostById(int id) {
        Post post = requirePostById(id);
        return buildPostResponse(post);
    }


    @LogExecutionTime
    public List<CommentResponse> getCommentsById(int id) {
        Post post = requirePostById(id);
        List<Comment> commentList = commentRepository.findAllByPost(post);

        return commentList.stream()
                .map(this::buildCommentResponse)
                .toList();
    }

    @LogExecutionTime
    public CommentResponse createComment(int id, CommentRequest commentRequest) {
        Post post = requirePostById(id);
        Comment comment = Comment.builder()
                .post(post)
                .authorName(commentRequest.getAuthor())
                .content(commentRequest.getContent())
                .build();
        Comment saved = commentRepository.insert(comment);

        discord.sendMessage(String.format("💬 게시글 ID: %d에 '%s'님이 댓글을 작성했습니다.\n내용: %s", id, commentRequest.getAuthor(), saved.getContent()));

        return buildCommentResponse(saved);
    }

    @LogExecutionTime
    public List<CategoryResponse> getCategories() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(cat -> {
                    int postCount = postRepository.countByCategoryId(cat.getId());
                    return CategoryResponse.builder()
                            .id(cat.getId())
                            .name(cat.getName())
                            .order(cat.getOrdernum())
                            .createdAt(cat.getCreatedAt())
                            .postCount(postCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @LogExecutionTime
    public List<TagResponse> getAllActiveTagsWithPosts() {
        List<Tag> tags = tagRepository.findAllByOrderByNameAsc().stream()
                .filter(tag -> !tag.getPosts().isEmpty())
                .collect(Collectors.toList());
        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    @LogExecutionTime
    public PostListResponse getPostsByTag(String tagName, int page, int size, String sort) {
        requireTagByName(tagName);

        SortCriteria sortCriteria = postSortBuilder.buildSortCriteria(sort);
        List<Post> posts = postRepository.findAllByTagName(tagName, page, size, sortCriteria.jpqlOrderClause());
        int totalElements = postRepository.countByTagName(tagName);

        return buildPostListResponse(posts, totalElements, page, size);
    }

    @LogExecutionTime
    public void incrementPostViews(int id) {
        Post post = requirePostById(id);
        post.setViews(post.getViews() + 1);
        postRepository.update(post);
    }

    private Post requirePostById(int id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            log.warn("Post with id {} not found", id);
            throw new NoSuchElementException("Post not found");
        }
        return post;
    }

    private Tag requireTagByName(String tagName) {
        return tagRepository.findByName(tagName)
                .orElseThrow(() -> new NoSuchElementException("Tag not found: " + tagName));
    }

    private PostResponse buildPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .summary(post.getSummary())
                .categoryId(post.getCategory().getId())
                .createdAt(post.getCreatedAt().toString())
                .updatedAt(post.getUpdatedAt().toString())
                .build();
    }

    private PostListResponse buildPostListResponse(List<Post> posts, int totalElements, int page, int size) {
        List<PostSummaryResponse> postSummaryList = posts.stream()
                .map(PostSummaryResponse::from)
                .collect(Collectors.toList());

        return PostListResponse.builder()
                .content(postSummaryList)
                .totalElements(totalElements)
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    private CommentResponse buildCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId().intValue())
                .authorName(comment.getAuthorName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt().toString())
                .build();
    }
}
