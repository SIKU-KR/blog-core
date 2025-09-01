package park.bumsiku.service;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import park.bumsiku.domain.dto.request.CreateCategoryRequest;
import park.bumsiku.domain.dto.request.CreatePostRequest;
import park.bumsiku.domain.dto.request.UpdateCategoryRequest;
import park.bumsiku.domain.dto.request.UpdatePostRequest;
import park.bumsiku.domain.dto.response.CategoryResponse;
import park.bumsiku.domain.dto.response.PostResponse;
import park.bumsiku.domain.dto.response.UploadImageResponse;
import park.bumsiku.domain.entity.Category;
import park.bumsiku.domain.entity.Comment;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.utils.monitoring.LogExecutionTime;
import park.bumsiku.repository.CategoryRepository;
import park.bumsiku.repository.CommentRepository;
import park.bumsiku.repository.ImageRepository;
import park.bumsiku.repository.PostRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class PrivateService {

    private CategoryRepository categoryRepository;
    private CommentRepository commentRepository;
    private PostRepository postRepository;
    private ImageRepository imageRepository;

    @LogExecutionTime
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setOrdernum(request.getOrderNum());

        Category createdCategory = categoryRepository.insert(category);

        return CategoryResponse.builder()
                .id(createdCategory.getId())
                .name(createdCategory.getName())
                .order(createdCategory.getOrdernum())
                .createdAt(createdCategory.getCreatedAt())
                .build();
    }

    @LogExecutionTime
    public CategoryResponse updateCategory(Integer id, UpdateCategoryRequest request) {
        Category existingCategory = categoryRepository.findById(id);

        if (existingCategory == null) {
            log.warn("Category not found with id: {}", id);
            throw new NoSuchElementException("Category not found with id: " + id);
        }

        // Update the category
        Category category = new Category();
        category.setId(id);
        category.setName(request.getName());
        category.setOrdernum(request.getOrderNum());

        Category updatedCategory = categoryRepository.update(category);

        return CategoryResponse.builder()
                .id(updatedCategory.getId())
                .name(updatedCategory.getName())
                .order(updatedCategory.getOrdernum())
                .createdAt(updatedCategory.getCreatedAt())
                .build();
    }

    @LogExecutionTime
    public void deleteComment(String commentId) {
        long id = Long.parseLong(commentId);

        Comment comment = commentRepository.findById(id);

        if (comment == null) {
            log.warn("Comment not found with id: {}", commentId);
            throw new NoSuchElementException("Comment not found with id: " + commentId);
        }

        commentRepository.delete(comment.getId());
    }

    @LogExecutionTime
    public UploadImageResponse uploadImage(MultipartFile image) {
        String filename = UUID.randomUUID() + ".webp";

        try (InputStream in = image.getInputStream()) {
            byte[] webpBytes = ImmutableImage.loader()
                    .fromStream(in)
                    .bytes(WebpWriter.DEFAULT);

            String url = imageRepository.insert(filename, webpBytes);

            return UploadImageResponse.builder()
                    .size(webpBytes.length)
                    .url(url)
                    .build();

        } catch (IOException e) {
            log.error("Image conversion and saving failed for file: {}", image.getOriginalFilename(), e);
            throw new RuntimeException("이미지 변환 및 저장 실패: " + e.getMessage(), e);
        }
    }

    @LogExecutionTime
    public PostResponse createPost(CreatePostRequest request) {
        Category category = categoryRepository.findById(request.getCategory());

        if (category == null) {
            log.warn("Category not found with id: {}", request.getCategory());
            throw new IllegalArgumentException("Category not found with id: " + request.getCategory());
        }

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .summary(request.getSummary())
                .category(category)
                .state("published")
                .build();

        Post savedPost = postRepository.insert(post);

        return PostResponse.builder()
                .id(savedPost.getId())
                .title(savedPost.getTitle())
                .content(savedPost.getContent())
                .createdAt(savedPost.getCreatedAt().toString())
                .updatedAt(savedPost.getUpdatedAt().toString())
                .build();
    }

    @LogExecutionTime
    public void deletePost(int postId) {
        Post post = postRepository.findById(postId);

        if (post == null) {
            log.warn("Post not found with id: {}", postId);
            throw new NoSuchElementException("Post not found with id: " + postId);
        }

        List<Comment> comments = commentRepository.findAllByPost(post);

        if (!comments.isEmpty()) {
            log.info("Deleting {} comments associated with post id: {}", comments.size(), postId);
            comments.forEach(comment ->
                    commentRepository.delete(comment.getId())
            );
        }

        postRepository.delete(postId);
    }

    @LogExecutionTime
    public PostResponse updatePost(int postId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId);

        if (post == null) {
            log.warn("Post not found with id: {}", postId);
            throw new NoSuchElementException("Post not found with id: " + postId);
        }

        Category category = categoryRepository.findById(request.getCategory());

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary());
        post.setCategory(category);
        post.setUpdatedAt(LocalDateTime.now());

        Post updatedPost = postRepository.update(post);

        return PostResponse.builder()
                .id(updatedPost.getId())
                .title(updatedPost.getTitle())
                .content(updatedPost.getContent())
                .createdAt(updatedPost.getCreatedAt().toString())
                .updatedAt(updatedPost.getUpdatedAt().toString())
                .build();
    }
}
