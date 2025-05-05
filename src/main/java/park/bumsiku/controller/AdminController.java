package park.bumsiku.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import park.bumsiku.common.ArgumentValidator;
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

    @Autowired
    private PrivateService service;

    @Autowired
    private ArgumentValidator validator;

    @Override
    @PostMapping("/categories")
    public Response<CategoryResponse> createCategory(
            @RequestBody CreateCategoryRequest request
    ) {
        validator.validateCategoryRequest(request);
        CategoryResponse categoryResponse = service.createCategory(request);
        return Response.success(categoryResponse);
    }

    @Override
    @PutMapping("/categories/{id}")
    public Response<CategoryResponse> updateCategory(
            @PathVariable Integer id,
            @RequestBody UpdateCategoryRequest request
    ) {
        validator.validateCategoryId(id);
        validator.validateCategoryRequest(request);
        CategoryResponse categoryResponse = service.updateCategory(id, request);
        return Response.success(categoryResponse);
    }

    @Override
    @DeleteMapping("/comments/{commentId}")
    public Response<Map<String, String>> deleteComment(
            @PathVariable String commentId
    ) {
        validator.validateCommentId(commentId);
        service.deleteComment(commentId);
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
        UploadImageResponse response = service.uploadImage(image);
        return Response.success(response);
    }

    @Override
    @PostMapping("/posts")
    public Response<PostResponse> addPost(
            @RequestBody CreatePostRequest request
    ) {
        validator.validatePostRequest(request);
        PostResponse postResponse = service.createPost(request);
        return Response.success(postResponse);
    }

    @Override
    @DeleteMapping("/posts/{postId}")
    public Response<Map<String, String>> deletePost(
            @PathVariable int postId
    ) {
        validator.validatePostId(postId);
        service.deletePost(postId);
        return Response.success(Map.of("message", "Post deleted successfully"));
    }

    @Override
    @PutMapping("/posts/{postId}")
    public Response<PostResponse> editPost(
            @PathVariable int postId,
            @RequestBody UpdatePostRequest request
    ) {
        validator.validatePostIdAndPostRequest(postId, request);
        PostResponse postResponse = service.updatePost(postId, request);
        return Response.success(postResponse);
    }
}
