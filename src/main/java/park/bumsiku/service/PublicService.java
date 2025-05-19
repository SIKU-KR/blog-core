package park.bumsiku.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import park.bumsiku.utils.DiscordWebhookCreator;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class PublicService {

    private static final Logger log = LoggerFactory.getLogger(PublicService.class);

    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private CategoryRepository categoryRepository;

    private DiscordWebhookCreator discord;

    public PostListResponse getPostList(int page, int size, String sort) {
        log.info("Fetching all posts with page: {}, size: {}, sort: {}", page, size, sort);
        List<PostSummaryResponse> postSummaryList = postRepository.findAll(page, size);
        int totalElements = postRepository.countAll();
        log.info("Successfully fetched {} posts out of total {}", postSummaryList.size(), totalElements);

        return PostListResponse.builder()
                .content(postSummaryList)
                .totalElements(totalElements)
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    public PostListResponse getPostList(int categoryId, int page, int size, String sort) {
        log.info("Fetching posts for category: {} with page: {}, size: {}, sort: {}", categoryId, page, size, sort);
        List<PostSummaryResponse> postSummaryList = postRepository.findAllByCategoryId(categoryId, page, size);
        int totalElements = postRepository.countByCategoryId(categoryId);
        log.info("Successfully fetched {} posts out of total {} for category: {}",
                postSummaryList.size(), totalElements, categoryId);

        return PostListResponse.builder()
                .content(postSummaryList)
                .totalElements(totalElements)
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    public PostResponse getPostById(int id) {
        log.info("Fetching post with id: {}", id);
        Post post = requirePostById(id);
        log.info("Successfully fetched post with title: {}", post.getTitle());

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

    public List<CommentResponse> getCommentsById(int id) {
        log.info("Fetching comments for post id: {}", id);
        Post post = requirePostById(id);
        List<Comment> commentList = commentRepository.findAllByPost(post);
        log.info("Successfully fetched {} comments for post id: {}", commentList.size(), id);

        return commentList.stream()
                .map(c -> CommentResponse.builder()
                        .id(c.getId().intValue())
                        .authorName(c.getAuthorName())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());
    }

    public CommentResponse createComment(int id, CommentRequest commentRequest) {
        log.info("Creating comment for post id: {} by author: {}", id, commentRequest.getAuthor());
        Post post = requirePostById(id);
        Comment comment = Comment.builder()
                .post(post)
                .authorName(commentRequest.getAuthor())
                .content(commentRequest.getContent())
                .build();
        Comment saved = commentRepository.insert(comment);
        log.info("Successfully created comment with id: {} for post id: {}", saved.getId(), id);
        discord.sendMessage(String.format("💬 게시글 ID: %d에 '%s'님이 댓글을 작성했습니다.\n내용: %s", id, commentRequest.getAuthor(), saved.getContent()));

        return CommentResponse.builder()
                .id(saved.getId().intValue())
                .authorName(saved.getAuthorName())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt().toString())
                .build();
    }

    public List<CategoryResponse> getCategories() {
        log.info("Fetching all categories");
        List<Category> categories = categoryRepository.findAll();
        log.info("Successfully fetched {} categories", categories.size());

        return categories.stream()
                .map(cat -> CategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .order(cat.getOrdernum())
                        .createdAt(cat.getCreatedAt())
                        .postCount(postRepository.countByCategoryId(cat.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private Post requirePostById(int id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new NoSuchElementException("Post not found");
        }
        return post;
    }
}
