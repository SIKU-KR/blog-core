package park.bumsiku.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import park.bumsiku.utils.ArgumentValidator;
import park.bumsiku.domain.dto.request.CreateCategoryRequest;
import park.bumsiku.domain.dto.request.CreatePostRequest;
import park.bumsiku.domain.dto.request.UpdateCategoryRequest;
import park.bumsiku.domain.dto.request.UpdatePostRequest;
import park.bumsiku.domain.dto.response.CategoryResponse;
import park.bumsiku.domain.dto.response.PostResponse;
import park.bumsiku.domain.dto.response.Response;
import park.bumsiku.domain.dto.response.UploadImageResponse;
import park.bumsiku.service.PrivateService;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController implements AdminAPI {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private PrivateService service;

    @Autowired
    private ArgumentValidator validator;

    @Override
    @PostMapping("/categories")
    public Response<CategoryResponse> createCategory(
            @RequestBody CreateCategoryRequest request
    ) {
        log.info("Creating new category: {}", request);
        validator.validateCategoryRequest(request);
        CategoryResponse categoryResponse = service.createCategory(request);
        log.info("Category created successfully: {}", categoryResponse);
        return Response.success(categoryResponse);
    }

    @Override
    @PutMapping("/categories/{id}")
    public Response<CategoryResponse> updateCategory(
            @PathVariable Integer id,
            @RequestBody UpdateCategoryRequest request
    ) {
        log.info("Updating category with id: {}, request: {}", id, request);
        validator.validateCategoryId(id);
        validator.validateCategoryRequest(request);
        CategoryResponse categoryResponse = service.updateCategory(id, request);
        log.info("Category updated successfully: {}", categoryResponse);
        return Response.success(categoryResponse);
    }

    @Override
    @DeleteMapping("/comments/{commentId}")
    public Response<Map<String, String>> deleteComment(
            @PathVariable String commentId
    ) {
        log.info("Deleting comment with id: {}", commentId);
        validator.validateCommentId(commentId);
        service.deleteComment(commentId);
        log.info("Comment deleted successfully: {}", commentId);
        return Response.success(Map.of("message", "Comment deleted successfully"));
    }

    @Override
    @PostMapping(
            value = "/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Response<UploadImageResponse> addImage(
            @RequestPart(value = "image", required = true) MultipartFile image
    ) {
        log.info("Uploading image: {}", image.getOriginalFilename());
        validator.validateImage(image);
        UploadImageResponse response = service.uploadImage(image);
        log.info("Image uploaded successfully: {}", response);
        return Response.success(response);
    }

    @Override
    @PostMapping("/posts")
    public Response<PostResponse> addPost(
            @RequestBody CreatePostRequest request
    ) {
        log.info("Creating new post: {}", request);
        validator.validatePostRequest(request);
        PostResponse postResponse = service.createPost(request);
        log.info("Post created successfully: {}", postResponse);
        return Response.success(postResponse);
    }

    @Override
    @DeleteMapping("/posts/{postId}")
    public Response<Map<String, String>> deletePost(
            @PathVariable int postId
    ) {
        log.info("Deleting post with id: {}", postId);
        validator.validatePostId(postId);
        service.deletePost(postId);
        log.info("Post deleted successfully: {}", postId);
        return Response.success(Map.of("message", "Post deleted successfully"));
    }

    @Override
    @PutMapping("/posts/{postId}")
    public Response<PostResponse> editPost(
            @PathVariable int postId,
            @RequestBody UpdatePostRequest request
    ) {
        log.info("Updating post with id: {}, request: {}", postId, request);
        validator.validatePostIdAndPostRequest(postId, request);
        PostResponse postResponse = service.updatePost(postId, request);
        log.info("Post updated successfully: {}", postResponse);
        return Response.success(postResponse);
    }
}
