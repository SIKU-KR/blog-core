package park.bumsiku.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import park.bumsiku.domain.dto.*;
import park.bumsiku.domain.entity.Category;
import park.bumsiku.domain.entity.Comment;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.exception.PostNotFoundException;
import park.bumsiku.repository.CategoryRepository;
import park.bumsiku.repository.CommentRepository;
import park.bumsiku.repository.PostRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PublicService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public PostListResponse getPostList(int page, int size, String sort) {
        List<PostSummaryResponse> postSummaryList = postRepository.findAll(page, size);
        return PostListResponse.builder()
                .content(postSummaryList)
                .totalElements(postSummaryList.size())
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    public PostListResponse getPostList(int categoryId, int page, int size, String sort) {
        List<PostSummaryResponse> postSummaryList = postRepository.findAllByCategoryId(categoryId, page, size);
        return PostListResponse.builder()
                .content(postSummaryList)
                .totalElements(postSummaryList.size())
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    public PostResponse getPostById(int id) {
        Post post = requirePostById(id);
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt().toString())
                .updatedAt(post.getUpdatedAt().toString())
                .build();
    }

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

    public CommentResponse createComment(int id, CommentRequest commentRequest) {
        Post post = requirePostById(id);
        Comment comment = Comment.builder()
                .post(post)
                .authorName(commentRequest.getAuthor())
                .content(commentRequest.getContent())
                .build();
        Comment saved = commentRepository.insert(comment);
        return CommentResponse.builder()
                .id(saved.getId().intValue())
                .authorName(saved.getAuthorName())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt().toString())
                .build();
    }

    public List<CategoryResponse> getCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(cat -> CategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .orderNum(cat.getOrderNum())
                        .createdAt(cat.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private Post requirePostById(int id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new PostNotFoundException("Post not found");
        }
        return post;
    }
}