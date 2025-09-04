package park.bumsiku.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import park.bumsiku.domain.dto.request.CreateCategoryRequest;
import park.bumsiku.domain.dto.request.CreatePostRequest;
import park.bumsiku.domain.dto.request.CreateTagRequest;
import park.bumsiku.domain.dto.request.UpdateCategoryRequest;
import park.bumsiku.domain.dto.request.UpdatePostRequest;
import park.bumsiku.domain.dto.request.UpdateTagRequest;
import park.bumsiku.domain.dto.response.CategoryResponse;
import park.bumsiku.domain.dto.response.PostResponse;
import park.bumsiku.domain.dto.response.Response;
import park.bumsiku.domain.dto.response.TagResponse;
import park.bumsiku.domain.dto.response.UploadImageResponse;
import park.bumsiku.service.PrivateService;
import park.bumsiku.service.TagService;
import park.bumsiku.utils.monitoring.LogExecutionTime;
import park.bumsiku.utils.validation.ArgumentValidator;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController implements AdminAPI {

    private PrivateService service;
    private TagService tagService;
    private ArgumentValidator validator;

    @Override
    @PostMapping("/categories")
    @LogExecutionTime
    public Response<CategoryResponse> createCategory(
            @RequestBody CreateCategoryRequest request
    ) {
        validator.validateCategoryRequest(request);
        CategoryResponse categoryResponse = service.createCategory(request);
        return Response.success(categoryResponse);
    }

    @Override
    @PutMapping("/categories/{id}")
    @LogExecutionTime
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
    @LogExecutionTime
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
    @LogExecutionTime
    public Response<UploadImageResponse> addImage(
            @RequestPart(value = "image", required = true) MultipartFile image
    ) {
        validator.validateImage(image);
        UploadImageResponse response = service.uploadImage(image);
        return Response.success(response);
    }

    @Override
    @PostMapping("/posts")
    @LogExecutionTime
    public Response<PostResponse> addPost(
            @RequestBody CreatePostRequest request
    ) {
        validator.validatePostRequest(request);
        PostResponse postResponse = service.createPost(request);
        return Response.success(postResponse);
    }

    @Override
    @DeleteMapping("/posts/{postId}")
    @LogExecutionTime
    public Response<Map<String, String>> deletePost(
            @PathVariable int postId
    ) {
        validator.validatePostId(postId);
        service.deletePost(postId);
        return Response.success(Map.of("message", "Post deleted successfully"));
    }

    @Override
    @PutMapping("/posts/{postId}")
    @LogExecutionTime
    public Response<PostResponse> editPost(
            @PathVariable int postId,
            @RequestBody UpdatePostRequest request
    ) {
        validator.validatePostIdAndPostRequest(postId, request);
        PostResponse postResponse = service.updatePost(postId, request);
        return Response.success(postResponse);
    }

    @Override
    @GetMapping("/tags")
    @LogExecutionTime
    public Response<List<TagResponse>> getAllTags() {
        List<TagResponse> tags = tagService.getAllTags();
        return Response.success(tags);
    }

    @Override
    @GetMapping("/tags/{tagId}")
    @LogExecutionTime
    public Response<TagResponse> getTagById(@PathVariable Integer tagId) {
        validator.validateTagId(tagId);
        TagResponse tagResponse = tagService.getTagById(tagId);
        return Response.success(tagResponse);
    }

    @Override
    @PostMapping("/tags")
    @LogExecutionTime
    public Response<TagResponse> createTag(@RequestBody CreateTagRequest request) {
        validator.validateTagRequest(request);
        TagResponse tagResponse = tagService.createTag(request);
        return Response.success(tagResponse);
    }

    @Override
    @PutMapping("/tags/{tagId}")
    @LogExecutionTime
    public Response<TagResponse> updateTag(
            @PathVariable Integer tagId,
            @RequestBody UpdateTagRequest request
    ) {
        validator.validateTagId(tagId);
        validator.validateTagRequest(request);
        TagResponse tagResponse = tagService.updateTag(tagId, request);
        return Response.success(tagResponse);
    }

    @Override
    @DeleteMapping("/tags/{tagId}")
    @LogExecutionTime
    public Response<Map<String, String>> deleteTag(@PathVariable Integer tagId) {
        validator.validateTagId(tagId);
        tagService.deleteTag(tagId);
        return Response.success(Map.of("message", "Tag deleted successfully"));
    }
}
