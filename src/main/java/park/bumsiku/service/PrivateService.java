package park.bumsiku.service;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class PrivateService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ImageRepository imageRepository;

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

    public CategoryResponse updateCategory(Integer id, UpdateCategoryRequest request) {
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

        return CategoryResponse.builder()
                .id(updatedCategory.getId())
                .name(updatedCategory.getName())
                .order(updatedCategory.getOrdernum())
                .createdAt(updatedCategory.getCreatedAt())
                .build();
    }

    public void deleteComment(String commentId) {
        long id = Long.parseLong(commentId);
        Comment comment = commentRepository.findById(id);

        if (comment == null) {
            throw new NoSuchElementException("Comment not found with id: " + commentId);
        }

        commentRepository.delete(comment.getId());
    }

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
            throw new RuntimeException("이미지 변환 및 저장 실패: " + e.getMessage(), e);
        }
    }

    public PostResponse createPost(CreatePostRequest request) {
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
        Post post = postRepository.findById(postId);

        if (post == null) {
            throw new NoSuchElementException("Post not found with id: " + postId);
        }

        // Delete all comments associated with the post
        commentRepository.findAllByPost(post).forEach(comment ->
                commentRepository.delete(comment.getId())
        );

        // Delete the post
        postRepository.delete(postId);
    }

    public PostResponse updatePost(int postId, UpdatePostRequest request) {
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
