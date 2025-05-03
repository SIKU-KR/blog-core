package park.bumsiku.service;

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
import park.bumsiku.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class PrivateService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

//    @Autowired
//    private PostImageRepository imageRepository;

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
        // TODO: business logic 구현
        throw new UnsupportedOperationException("Not implemented");
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

    /**
     * Helper method to find a category by name
     *
     * @param categoryName the name of the category to find
     * @return the found category or the default category (ID 1) if not found
     */
    private Category findCategoryByName(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return categoryRepository.findById(1); // Default to ID 1 if name is null or empty
        }

        // Find all categories and filter by name
        List<Category> categories = categoryRepository.findAll();
        for (Category category : categories) {
            if (categoryName.equals(category.getName())) {
                return category;
            }
        }

        // Default to ID 1 if not found
        return categoryRepository.findById(1);
    }

    public PostResponse updatePost(int postId, UpdatePostRequest request) {
        // Find the post
        Post post = postRepository.findById(postId);

        if (post == null) {
            throw new NoSuchElementException("Post not found with id: " + postId);
        }

        // Find the category by name
        Category category = findCategoryByName(request.getCategory());

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
