package park.bumsiku.controller;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import park.bumsiku.utils.ArgumentValidator;
import park.bumsiku.domain.dto.request.CommentRequest;
import park.bumsiku.domain.dto.response.*;
import park.bumsiku.service.PublicService;

import java.util.List;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class PublicController implements PublicAPI {

    private static final Logger log = LoggerFactory.getLogger(PublicController.class);

    private PublicService service;
    private ArgumentValidator validator;

    @Override
    @GetMapping("/")
    public RedirectView redirectToSwagger() {
        log.info("Redirecting to Swagger UI");
        return new RedirectView("/swagger-ui/index.html");
    }

    @Override
    @GetMapping("/posts")
    public Response<PostListResponse> getPosts(
            @RequestParam(value = "category", required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        log.info("Fetching posts with categoryId: {}, page: {}, size: {}, sort: {}",
                categoryId, page, size, sort);

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
        log.info("Successfully fetched {} posts", result.getTotalElements());
        return Response.success(result);
    }

    @Override
    @GetMapping("/posts/{postId}")
    public Response<PostResponse> getPostById(
            @PathVariable("postId") int postId) {

        log.info("Fetching post with id: {}", postId);
        validator.validatePostId(postId);

        PostResponse result = service.getPostById(postId);
        log.info("Successfully fetched post: {}", result);
        return Response.success(result);
    }

    @Override
    @GetMapping("/comments/{postId}")
    public Response<List<CommentResponse>> getCommentsByPostId(
            @PathVariable("postId") int postId) {

        log.info("Fetching comments for post id: {}", postId);
        validator.validatePostId(postId);

        List<CommentResponse> result = service.getCommentsById(postId);
        log.info("Successfully fetched {} comments for post id: {}", result.size(), postId);
        return Response.success(result);
    }

    @Override
    @PostMapping("/comments/{postId}")
    public Response<CommentResponse> postComment(
            @PathVariable("postId") int postId,
            @RequestBody CommentRequest commentRequest) {

        log.info("Creating comment for post id: {}, comment: {}", postId, commentRequest);
        validator.validatePostIdAndCommentRequest(postId, commentRequest);

        CommentResponse result = service.createComment(postId, commentRequest);
        log.info("Successfully created comment: {}", result);
        return Response.success(result);
    }

    @Override
    @GetMapping("/categories")
    public Response<List<CategoryResponse>> getCategories() {
        log.info("Fetching all categories");
        List<CategoryResponse> result = service.getCategories();
        log.info("Successfully fetched {} categories", result.size());
        return Response.success(result);
    }
}
