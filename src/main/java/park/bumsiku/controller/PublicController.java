package park.bumsiku.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import park.bumsiku.domain.dto.request.CommentRequest;
import park.bumsiku.domain.dto.response.*;
import park.bumsiku.service.PublicService;
import park.bumsiku.utils.monitoring.LogExecutionTime;
import park.bumsiku.utils.validation.ArgumentValidator;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/")
@AllArgsConstructor
public class PublicController implements PublicAPI {

    private PublicService service;
    private ArgumentValidator validator;

    @Override
    @GetMapping("/")
    @LogExecutionTime
    public RedirectView redirectToSwagger() {
        return new RedirectView("/swagger-ui/index.html");
    }

    @Override
    @GetMapping("/posts")
    @LogExecutionTime
    public Response<PostListResponse> getPosts(
            @RequestParam(value = "tag", required = false) String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        validator.validatePagination(page, size);
        PostListResponse result;
        if (tagName != null && !tagName.isBlank()) {
            result = service.getPostsByTag(tagName, page, size, sort);
        } else {
            result = service.getPostList(page, size, sort);
        }
        return Response.success(result);
    }

    @Override
    @GetMapping(value = "/posts/{slug}")
    @LogExecutionTime
    public Object getPostBySlugOrRedirect(
            @PathVariable("slug") String slug) {

        // 숫자인 경우 리다이렉트
        if (slug.matches("\\d+")) {
            int postId = Integer.parseInt(slug);
            validator.validatePostId(postId);
            String resolvedSlug = service.resolveSlugById(postId);

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.LOCATION, "/posts/" + resolvedSlug);
            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        }

        validator.validateSlug(slug);
        PostResponse result = service.getPostBySlug(slug);
        return Response.success(result);
    }


    @Override
    @GetMapping("/comments/{postId}")
    @LogExecutionTime
    public Response<List<CommentResponse>> getCommentsByPostId(
            @PathVariable("postId") int postId) {

        validator.validatePostId(postId);

        List<CommentResponse> result = service.getCommentsById(postId);

        return Response.success(result);
    }

    @Override
    @PostMapping("/comments/{postId}")
    @LogExecutionTime
    public Response<CommentResponse> postComment(
            @PathVariable("postId") int postId,
            @RequestBody CommentRequest commentRequest) {

        validator.validatePostIdAndCommentRequest(postId, commentRequest);

        CommentResponse result = service.createComment(postId, commentRequest);

        return Response.success(result);
    }


    @Override
    @PatchMapping("/posts/{postId}/views")
    @LogExecutionTime
    public Response<Void> incrementPostViews(
            @PathVariable("postId") int postId) {

        validator.validatePostId(postId);

        service.incrementPostViews(postId);

        return Response.success(null);
    }

    @Override
    @GetMapping("/tags")
    @LogExecutionTime
    public Response<List<TagResponse>> getTags() {
        List<TagResponse> tags = service.getAllActiveTagsWithPosts();
        return Response.success(tags);
    }

    @Override
    @GetMapping("/sitemap")
    @LogExecutionTime
    public Response<List<String>> getSitemapPaths() {
        List<String> paths = service.getCanonicalPaths();
        return Response.success(paths);
    }
}
