package park.bumsiku.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import park.bumsiku.domain.dto.request.CreateCategoryRequest;
import park.bumsiku.domain.dto.request.CreatePostRequest;
import park.bumsiku.domain.dto.request.UpdateCategoryRequest;
import park.bumsiku.domain.dto.request.UpdatePostRequest;
import park.bumsiku.domain.dto.response.CategoryResponse;
import park.bumsiku.domain.dto.response.PostResponse;
import park.bumsiku.domain.dto.response.Response;
import park.bumsiku.domain.dto.response.UploadImageResponse;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin API", description = "관리자 전용 API")
@SecurityRequirement(name = "AdminAuth")
public interface AdminAPI {

    @Operation(
            summary = "카테고리 추가",
            description = "새로운 블로그 카테고리를 추가합니다 (관리자 전용)"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Created",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CategoryResponse.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PostMapping("/admin/categories")
    Response<CategoryResponse> createCategory(
            @Parameter(description = "카테고리 정보")
            @RequestBody CreateCategoryRequest request
    );

    @Operation(
            summary = "카테고리 수정",
            description = "기존 블로그 카테고리를 수정합니다 (관리자 전용)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CategoryResponse.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PutMapping("/admin/categories/{id}")
    Response<CategoryResponse> updateCategory(
            @Parameter(description = "수정할 카테고리 ID")
            @PathVariable Integer id,
            @Parameter(description = "카테고리 정보")
            @RequestBody UpdateCategoryRequest request
    );

    @Operation(
            summary = "댓글 삭제",
            description = "블로그 댓글을 삭제합니다 (관리자 전용)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "삭제 성공 메시지",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @DeleteMapping("/admin/comments/{commentId}")
    Response<Map<String, String>> deleteComment(
            @Parameter(description = "삭제할 댓글 ID")
            @PathVariable String commentId
    );

    @Operation(
            summary = "이미지 업로드",
            description = "블로그에 표시할 이미지를 업로드합니다 (관리자 전용)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "업로드 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UploadImageResponse.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PostMapping(
            value = "/admin/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    Response<UploadImageResponse> addImage(
            @Parameter(
                    description = "이미지 파일",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart(value = "image", required = true) MultipartFile image
    );

    @Operation(
            summary = "게시물 작성",
            description = "새 블로그 게시물을 작성합니다 (관리자 전용)"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Created",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PostResponse.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PostMapping("/admin/posts")
    Response<PostResponse> addPost(
            @Parameter(description = "게시물 정보")
            @RequestBody CreatePostRequest request
    );

    @Operation(
            summary = "게시물 삭제",
            description = "블로그 게시물과 관련 댓글을 삭제합니다 (관리자 전용)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "삭제 성공 메시지",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @DeleteMapping("/admin/posts/{postId}")
    Response<Map<String, String>> deletePost(
            @Parameter(description = "삭제할 게시물 ID")
            @PathVariable int postId
    );

    @Operation(
            summary = "게시물 수정",
            description = "기존 블로그 게시물을 수정합니다 (관리자 전용)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PostResponse.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PutMapping("/admin/posts/{postId}")
    Response<PostResponse> editPost(
            @Parameter(description = "수정할 게시물 ID")
            @PathVariable int postId,
            @Parameter(description = "수정할 게시물 정보")
            @RequestBody UpdatePostRequest request
    );

}
