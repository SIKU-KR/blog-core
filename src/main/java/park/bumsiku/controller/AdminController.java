package park.bumsiku.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import park.bumsiku.domain.dto.request.CreatePostRequest;
import park.bumsiku.domain.dto.request.UpdateCategoryRequest;
import park.bumsiku.domain.dto.request.UpdatePostRequest;
import park.bumsiku.domain.dto.response.CategoryResponse;
import park.bumsiku.domain.dto.response.PostResponse;
import park.bumsiku.domain.dto.response.Response;
import park.bumsiku.domain.dto.response.UploadImageResponse;
import park.bumsiku.service.PrivateService;
import park.bumsiku.validator.ArgumentValidator;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController implements AdminAPI {

    @Autowired
    private PrivateService service;

    @Autowired
    private ArgumentValidator validator;

    @Override
    @PutMapping("/categories")
    public Response<CategoryResponse> putCategory(
            @RequestBody UpdateCategoryRequest request
    ) {
        validator.validateCategoryRequest(request);

        // TODO: 관리자용 카테고리 추가/수정 로직 구현
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    @DeleteMapping("/comments/{commentId}")
    public Response<Map<String, String>> deleteComment(
            @PathVariable String commentId
    ) {
        validator.validateCommentId(commentId);

        // TODO: 댓글 삭제 로직 구현
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    @PostMapping(
            value = "/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Response<UploadImageResponse> addImage(
            @RequestPart(value = "image", required = true) MultipartFile image
    ) {
        // TODO: 이미지 업로드 로직 구현
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    @PostMapping("/posts")
    public Response<PostResponse> addPost(
            @RequestBody CreatePostRequest request
    ) {
        validator.validatePostRequest(request);

        // TODO: 게시물 작성 로직 구현
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    @DeleteMapping("/posts/{postId}")
    public Response<Map<String, String>> deletePost(
            @PathVariable int postId
    ) {
        validator.validatePostId(postId);

        // TODO: 게시물 삭제 로직 구현
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    @PutMapping("/posts/{postId}")
    public Response<PostResponse> editPost(
            @PathVariable int postId,
            @RequestBody UpdatePostRequest request
    ) {
        validator.validatePostIdAndPostRequest(postId, request);

        // TODO: 게시물 수정 로직 구현
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
