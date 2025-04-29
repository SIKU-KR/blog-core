package park.bumsiku.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import park.bumsiku.domain.dto.*;
import park.bumsiku.service.PublicService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/")
public class PublicController implements PublicAPI {

    @Autowired
    private PublicService service;

    @Override
    @GetMapping("/")
    public RedirectView redirectToSwagger() {
        return new RedirectView("/swagger-ui.html");
    }

    @Override
    @GetMapping("/posts")
    public Response<PostListResponse> getPosts(
            @RequestParam(value = "category", required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        PostListResponse result;
        if (categoryId != null) {
            result = service.getPostList(categoryId, page, size, sort);
        } else {
            result = service.getPostList(page, size, sort);
        }
        return Response.success(result);
    }

    @Override
    @GetMapping("/posts/{postId}")
    public Response<PostResponse> getPostById(
            @PathVariable("postId")
            @Min(value = 1, message = "게시글 ID는 1 이상이어야 합니다") int postId) {
        PostResponse result = service.getPostById(postId);
        return Response.success(result);
    }

    @Override
    @GetMapping("/comments/{postId}")
    public Response<List<CommentResponse>> getCommentsByPostId(
            @PathVariable("postId")
            @Min(value = 1, message = "게시글 ID는 1 이상이어야 합니다") int postId) {
        List<CommentResponse> result = service.getCommentsById(postId);
        return Response.success(result);
    }

    @Override
    @PostMapping("/comments/{postId}")
    public Response<CommentResponse> postComment(
            @PathVariable("postId")
            @Min(value = 1, message = "게시글 ID는 1 이상이어야 합니다") int postId,
            @Valid @RequestBody CommentRequest commentRequest) {
        CommentResponse result = service.createComment(postId, commentRequest);
        return Response.success(result);
    }

    @Override
    @GetMapping("/categories")
    public Response<List<CategoryResponse>> getCategories() {
        List<CategoryResponse> result = service.getCategories();
        return Response.success(result);
    }
}