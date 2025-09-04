package park.bumsiku.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import park.bumsiku.domain.dto.request.CommentRequest;
import park.bumsiku.domain.dto.response.*;

import java.util.List;

@Tag(name = "Public API", description = "공개적으로 접근 가능한 API")
public interface PublicAPI {

    @Operation(
            summary = "Swagger UI 리디렉션",
            description = "루트 경로 접근 시 Swagger UI 페이지로 리디렉션합니다."
    )
    @ApiResponse(responseCode = "302", description = "Found - Swagger UI 페이지로 리디렉션")
    @GetMapping("/")
    RedirectView redirectToSwagger();

    @Operation(
            summary = "게시글 목록 조회",
            description = "페이지네이션 및 정렬 기능 제공"
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Response.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (페이지 또는 사이즈 파라미터 오류)")
    @GetMapping("/posts")
    Response<PostListResponse> getPosts(
            @RequestParam(value = "category", required = false) Integer categoryId,
            @RequestParam(value = "tag", required = false) String tagName,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 (예: createdAt,desc)")
            @RequestParam(defaultValue = "createdAt,desc") String sort
    );

    @Operation(
            summary = "특정 게시글 조회",
            description = "ID를 이용하여 특정 게시글 상세 정보를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Response.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (ID 형식 오류)")
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    @GetMapping("/posts/{postId}")
    Response<PostResponse> getPostById(
            @Parameter(description = "조회할 게시글 ID")
            @PathVariable("postId") int postId
    );

    @Operation(
            summary = "특정 게시글의 댓글 목록 조회",
            description = "게시글 ID를 이용하여 해당 게시글의 모든 댓글을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Response.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (ID 형식 오류)")
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    @GetMapping("/comments/{postId}")
    Response<List<CommentResponse>> getCommentsByPostId(
            @Parameter(description = "댓글을 조회할 게시글 ID")
            @PathVariable("postId") int postId
    );

    @Operation(
            summary = "댓글 작성",
            description = "특정 게시글에 새로운 댓글을 작성합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Response.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청 본문 오류)")
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    @PostMapping("/comments/{postId}")
    Response<CommentResponse> postComment(
            @Parameter(description = "댓글을 작성할 게시글 ID")
            @PathVariable("postId") int postId,
            @RequestBody CommentRequest commentRequest
    );

    @Operation(
            summary = "카테고리 목록 조회",
            description = "블로그에 등록된 모든 카테고리와 각 카테고리에 속한 게시글 수를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Response.class)
            )
    )
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/categories")
    Response<List<CategoryResponse>> getCategories();

    @Operation(
            summary = "게시글 조회수 증가",
            description = "특정 게시글의 조회수를 1 증가시킵니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Response.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (ID 형식 오류)")
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    @PatchMapping("/posts/{postId}/views")
    Response<Void> incrementPostViews(
            @Parameter(description = "조회수를 증가시킬 게시글 ID")
            @PathVariable("postId") int postId
    );

    @Operation(
            summary = "태그 목록 조회",
            description = "블로그에 등록된 모든 태그와 각 태그에 연결된 게시글 수를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Response.class)
            )
    )
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/tags")
    Response<List<TagResponse>> getTags();

    // 이전: /posts/by-tag는 /posts?tag= 로 통합되었습니다.
}
