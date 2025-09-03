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
import park.bumsiku.repository.CategoryRepository;
import park.bumsiku.repository.CommentRepository;
import park.bumsiku.repository.PostRepository;
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
    private PostSortBuilder postSortBuilder;

    private DiscordWebhookCreator discord;

    @LogExecutionTime
    public PostListResponse getPostList(int page, int size, String sort) {
        SortCriteria sortCriteria = postSortBuilder.buildSortCriteria(sort);
        List<Post> posts = postRepository.findAll(page, size, sortCriteria.getJpqlOrderClause());
        List<PostSummaryResponse> postSummaryList = posts.stream()
                .map(PostSummaryResponse::from)
                .collect(Collectors.toList());
        int totalElements = postRepository.countAll();

        return PostListResponse.builder()
                .content(postSummaryList)
                .totalElements(totalElements)
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    @LogExecutionTime
    public PostListResponse getPostList(int categoryId, int page, int size, String sort) {
        SortCriteria sortCriteria = postSortBuilder.buildSortCriteria(sort);
        List<Post> posts = postRepository.findAllByCategoryId(categoryId, page, size, sortCriteria.getJpqlOrderClause());
        List<PostSummaryResponse> postSummaryList = posts.stream()
                .map(PostSummaryResponse::from)
                .collect(Collectors.toList());
        int totalElements = postRepository.countByCategoryId(categoryId);

        return PostListResponse.builder()
                .content(postSummaryList)
                .totalElements(totalElements)
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    @LogExecutionTime
    public PostResponse getPostById(int id) {
        Post post = requirePostById(id);

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

    @LogExecutionTime
    public List<CommentResponse> getCommentsById(int id) {
        Post post = requirePostById(id);
        List<Comment> commentList = commentRepository.findAllByPost(post);

        return commentList.stream()
                .map(c -> CommentResponse.builder()
                        .id(c.getId().intValue())
                        .authorName(c.getAuthorName())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());
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

        return CommentResponse.builder()
                .id(saved.getId().intValue())
                .authorName(saved.getAuthorName())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt().toString())
                .build();
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
}
