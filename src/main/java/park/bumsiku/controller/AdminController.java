package park.bumsiku.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import park.bumsiku.domain.dto.*;
import park.bumsiku.service.PrivateService;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminController implements AdminAPI {

    @Autowired
    private PrivateService service;

    @Override
    @PutMapping("/categories")
    public Response<CategoryResponse> putCategory(
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        // TODO: 관리자용 카테고리 추가/수정 로직 구현
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    @DeleteMapping("/comments/{commentId}")
    public Response<Map<String, String>> deleteComment(
            @PathVariable
            @Min(value = 1, message = "댓글 ID는 1 이상이어야 합니다")
            String commentId
    ) {
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
            @Valid @RequestBody CreatePostRequest request
    ) {
        // TODO: 게시물 작성 로직 구현
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    @DeleteMapping("/posts/{postId}")
    public Response<Map<String, String>> deletePost(
            @PathVariable
            @Min(value = 1, message = "게시글 ID는 1 이상이어야 합니다")
            int postId
    ) {
        // TODO: 게시물 삭제 로직 구현
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    @PutMapping("/posts/{postId}")
    public Response<PostResponse> editPost(
            @PathVariable
            @Min(value = 1, message = "게시글 ID는 1 이상이어야 합니다")
            int postId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        // TODO: 게시물 수정 로직 구현
        throw new UnsupportedOperationException("Not yet implemented");
    }
}