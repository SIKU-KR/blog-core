package park.bumsiku.service;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
@Transactional
@AllArgsConstructor
public class PrivateService {

    private static final Logger log = LoggerFactory.getLogger(PrivateService.class);

    private CategoryRepository categoryRepository;
    private CommentRepository commentRepository;
    private PostRepository postRepository;
    private ImageRepository imageRepository;

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating new category with name: {}, orderNum: {}", request.getName(), request.getOrderNum());
        Category category = new Category();
        category.setName(request.getName());
        category.setOrdernum(request.getOrderNum());

        Category createdCategory = categoryRepository.insert(category);
        log.info("Successfully created category with id: {}", createdCategory.getId());

        return CategoryResponse.builder()
                .id(createdCategory.getId())
                .name(createdCategory.getName())
                .order(createdCategory.getOrdernum())
                .createdAt(createdCategory.getCreatedAt())
                .build();
    }

    public CategoryResponse updateCategory(Integer id, UpdateCategoryRequest request) {
        log.info("Updating category with id: {}, name: {}, orderNum: {}", id, request.getName(), request.getOrderNum());
        // Check if the category exists
        Category existingCategory = categoryRepository.findById(id);
        if (existingCategory == null) {
            throw new NoSuchElementException("Category not found with id: " + id);
        }

        // Update the category
        Category category = new Category();
        category.setId(id);
        category.setName(request.getName());
        category.setOrdernum(request.getOrderNum());

        Category updatedCategory = categoryRepository.update(category);
        log.info("Successfully updated category with id: {}", updatedCategory.getId());

        return CategoryResponse.builder()
                .id(updatedCategory.getId())
                .name(updatedCategory.getName())
                .order(updatedCategory.getOrdernum())
                .createdAt(updatedCategory.getCreatedAt())
                .build();
    }

    public void deleteComment(String commentId) {
        log.info("Deleting comment with id: {}", commentId);
        long id = Long.parseLong(commentId);
        Comment comment = commentRepository.findById(id);

        if (comment == null) {
            throw new NoSuchElementException("Comment not found with id: " + commentId);
        }

        commentRepository.delete(comment.getId());
        log.info("Successfully deleted comment with id: {}", commentId);
    }

    public UploadImageResponse uploadImage(MultipartFile image) {
        log.info("Uploading image with original filename: {}", image.getOriginalFilename());
        String filename = UUID.randomUUID() + ".webp";

        try (InputStream in = image.getInputStream()) {
            byte[] webpBytes = ImmutableImage.loader()
                    .fromStream(in)
                    .bytes(WebpWriter.DEFAULT);

            String url = imageRepository.insert(filename, webpBytes);
            log.info("Successfully uploaded image. URL: {}, Size: {} bytes", url, webpBytes.length);

            return UploadImageResponse.builder()
                    .size(webpBytes.length)
                    .url(url)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("이미지 변환 및 저장 실패: " + e.getMessage(), e);
        }
    }

    public PostResponse createPost(CreatePostRequest request) {
        log.info("Creating new post with title: {}, category: {}", request.getTitle(), request.getCategory());
        // Find the category by name
        Category category = categoryRepository.findById(request.getCategory());
        if (category == null) {
            throw new IllegalArgumentException("Category not found with id: " + request.getCategory());
        }

        // Create a new post
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .summary(request.getSummary())
                .category(category)
                .state("published")
                .build();

        // Save the post
        Post savedPost = postRepository.insert(post);
        log.info("Successfully created post with id: {}", savedPost.getId());

        // Create and return the response
        return PostResponse.builder()
                .id(savedPost.getId())
                .title(savedPost.getTitle())
                .content(savedPost.getContent())
                .createdAt(savedPost.getCreatedAt().toString())
                .updatedAt(savedPost.getUpdatedAt().toString())
                .build();
    }

    public void deletePost(int postId) {
        log.info("Deleting post with id: {}", postId);
        Post post = postRepository.findById(postId);

        if (post == null) {
            throw new NoSuchElementException("Post not found with id: " + postId);
        }

        // Delete all comments associated with the post
        List<Comment> comments = commentRepository.findAllByPost(post);
        log.info("Deleting {} comments associated with post id: {}", comments.size(), postId);
        comments.forEach(comment ->
                commentRepository.delete(comment.getId())
        );

        // Delete the post
        postRepository.delete(postId);
        log.info("Successfully deleted post with id: {}", postId);
    }

    public PostResponse updatePost(int postId, UpdatePostRequest request) {
        log.info("Updating post with id: {}, title: {}, category: {}", postId, request.getTitle(), request.getCategory());
        // Find the post
        Post post = postRepository.findById(postId);

        if (post == null) {
            throw new NoSuchElementException("Post not found with id: " + postId);
        }

        // Find the category by name
        Category category = categoryRepository.findById(request.getCategory());

        // Update the post
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary());
        post.setCategory(category);
        post.setUpdatedAt(LocalDateTime.now());

        // Save the updated post
        Post updatedPost = postRepository.update(post);
        log.info("Successfully updated post with id: {}", updatedPost.getId());

        // Create and return the response
        return PostResponse.builder()
                .id(updatedPost.getId())
                .title(updatedPost.getTitle())
                .content(updatedPost.getContent())
                .createdAt(updatedPost.getCreatedAt().toString())
                .updatedAt(updatedPost.getUpdatedAt().toString())
                .build();
    }
}
