package park.bumsiku.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import park.bumsiku.domain.dto.request.CommentRequest;
import park.bumsiku.domain.dto.response.*;
import park.bumsiku.service.PublicService;
import park.bumsiku.common.ArgumentValidator;

import java.util.List;

@RestController
@RequestMapping("/")
public class PublicController implements PublicAPI {

    @Autowired
    private PublicService service;

    @Autowired
    private ArgumentValidator validator;

    @Override
    @GetMapping("/")
    public RedirectView redirectToSwagger() {
        return new RedirectView("/swagger-ui/index.html");
    }

    @Override
    @GetMapping("/posts")
    public Response<PostListResponse> getPosts(
            @RequestParam(value = "category", required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        validator.validatePagination(page, size);
        if (categoryId != null) {
            validator.validateCategoryId(categoryId);
        }

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
            @PathVariable("postId") int postId) {

        validator.validatePostId(postId);

        PostResponse result = service.getPostById(postId);
        return Response.success(result);
    }

    @Override
    @GetMapping("/comments/{postId}")
    public Response<List<CommentResponse>> getCommentsByPostId(
            @PathVariable("postId") int postId) {

        validator.validatePostId(postId);

        List<CommentResponse> result = service.getCommentsById(postId);
        return Response.success(result);
    }

    @Override
    @PostMapping("/comments/{postId}")
    public Response<CommentResponse> postComment(
            @PathVariable("postId") int postId,
            @RequestBody CommentRequest commentRequest) {

        validator.validatePostIdAndCommentRequest(postId, commentRequest);

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
