package park.bumsiku.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import park.bumsiku.utils.ArgumentValidator;
import park.bumsiku.config.Security;
import park.bumsiku.domain.dto.request.CommentRequest;
import park.bumsiku.domain.dto.response.*;
import park.bumsiku.service.PublicService;
import park.bumsiku.utils.DiscordWebhookCreator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicController.class)
@Import(Security.class)
public class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicService publicService;

    @MockitoBean
    private ArgumentValidator validator;

    @MockitoBean
    private DiscordWebhookCreator webhookCreator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRedirectToSwagger() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }

    @Test
    public void testGetPosts_Success() throws Exception {
        // Prepare test data
        PostSummaryResponse post1 = PostSummaryResponse.builder()
                .id(1)
                .title("Test Post 1")
                .summary("Summary 1")
                .build();

        PostSummaryResponse post2 = PostSummaryResponse.builder()
                .id(2)
                .title("Test Post 2")
                .summary("Summary 2")
                .build();

        List<PostSummaryResponse> posts = Arrays.asList(post1, post2);
        PostListResponse postListResponse = PostListResponse.builder()
                .content(posts)
                .totalElements(2)
                .pageNumber(0)
                .pageSize(10)
                .build();

        // Mock service response
        when(publicService.getPostList(anyInt(), anyInt(), anyString()))
                .thenReturn(postListResponse);

        // Perform request and verify
        mockMvc.perform(get("/posts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].title", is("Test Post 1")))
                .andExpect(jsonPath("$.data.content[1].title", is("Test Post 2")));
    }

    @Test
    public void testGetPosts_WithCategory_Success() throws Exception {
        // Prepare test data
        PostSummaryResponse post = PostSummaryResponse.builder()
                .id(1)
                .title("Test Post")
                .summary("Summary")
                .build();

        List<PostSummaryResponse> posts = Collections.singletonList(post);
        PostListResponse postListResponse = PostListResponse.builder()
                .content(posts)
                .totalElements(1)
                .pageNumber(0)
                .pageSize(10)
                .build();

        // Mock service response
        when(publicService.getPostList(eq(1), anyInt(), anyInt(), anyString()))
                .thenReturn(postListResponse);

        // Perform request and verify
        mockMvc.perform(get("/posts")
                        .param("category", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title", is("Test Post")));
    }

    @Test
    public void testGetPosts_InvalidPagination() throws Exception {
        // Mock validator to throw exception
        doThrow(new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다"))
                .when(publicService).getPostList(eq(-1), anyInt(), anyString());

        // Perform request and verify
        mockMvc.perform(get("/posts")
                        .param("page", "-1")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("페이지 번호는 0 이상이어야 합니다")));
    }

    @Test
    public void testGetPostById_Success() throws Exception {
        // Prepare test data
        PostResponse postResponse = PostResponse.builder()
                .id(1)
                .title("Test Post")
                .content("Test Content")
                .createdAt("2023-01-01T12:00:00")
                .updatedAt("2023-01-01T12:00:00")
                .build();

        // Mock service response
        when(publicService.getPostById(1)).thenReturn(postResponse);

        // Perform request and verify
        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.title", is("Test Post")))
                .andExpect(jsonPath("$.data.content", is("Test Content")));
    }

    @Test
    public void testGetPostById_NotFound() throws Exception {
        // Mock service to throw exception
        when(publicService.getPostById(999)).thenThrow(new NoSuchElementException("게시글을 찾을 수 없습니다"));

        // Perform request and verify
        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(404)))
                .andExpect(jsonPath("$.error.message", is("게시글을 찾을 수 없습니다")));
    }

    @Test
    public void testGetCommentsByPostId_Success() throws Exception {
        // Prepare test data
        CommentResponse comment1 = CommentResponse.builder()
                .id(1)
                .authorName("Author 1")
                .content("Comment 1")
                .createdAt("2023-01-01T12:00:00")
                .build();

        CommentResponse comment2 = CommentResponse.builder()
                .id(2)
                .authorName("Author 2")
                .content("Comment 2")
                .createdAt("2023-01-01T12:00:00")
                .build();

        List<CommentResponse> comments = Arrays.asList(comment1, comment2);

        // Mock service response
        when(publicService.getCommentsById(1)).thenReturn(comments);

        // Perform request and verify
        mockMvc.perform(get("/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].authorName", is("Author 1")))
                .andExpect(jsonPath("$.data[1].authorName", is("Author 2")));
    }

    @Test
    public void testPostComment_Success() throws Exception {
        // Prepare test data
        CommentRequest commentRequest = CommentRequest.builder()
                .author("Test Author")
                .content("Test Comment")
                .build();

        CommentResponse commentResponse = CommentResponse.builder()
                .id(1)
                .authorName("Test Author")
                .content("Test Comment")
                .createdAt("2023-01-01T12:00:00")
                .build();

        // Mock service response
        when(publicService.createComment(eq(1), any(CommentRequest.class))).thenReturn(commentResponse);

        // Perform request and verify
        mockMvc.perform(post("/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.authorName", is("Test Author")))
                .andExpect(jsonPath("$.data.content", is("Test Comment")));
    }

    @Test
    public void testPostComment_InvalidRequest() throws Exception {
        // Prepare invalid test data (empty author)
        CommentRequest commentRequest = CommentRequest.builder()
                .author("")
                .content("Test Comment")
                .build();

        // Mock service to throw exception
        doThrow(new IllegalArgumentException("작성자 이름을 입력해주세요"))
                .when(publicService).createComment(eq(1), any(CommentRequest.class));

        // Perform request and verify
        mockMvc.perform(post("/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("작성자 이름을 입력해주세요")));
    }

    @Test
    public void testGetCategories_Success() throws Exception {
        // Prepare test data
        CategoryResponse category1 = CategoryResponse.builder()
                .id(1)
                .name("Category 1")
                .order(1)
                .createdAt(LocalDateTime.now())
                .postCount(5)
                .build();

        CategoryResponse category2 = CategoryResponse.builder()
                .id(2)
                .name("Category 2")
                .order(2)
                .createdAt(LocalDateTime.now())
                .postCount(3)
                .build();

        List<CategoryResponse> categories = Arrays.asList(category1, category2);

        // Mock service response
        when(publicService.getCategories()).thenReturn(categories);

        // Perform request and verify
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name", is("Category 1")))
                .andExpect(jsonPath("$.data[0].postCount", is(5)))
                .andExpect(jsonPath("$.data[1].name", is("Category 2")))
                .andExpect(jsonPath("$.data[1].postCount", is(3)));
    }

    // Additional tests for missing HTTP status codes

    @Test
    public void testGetPostById_BadRequest() throws Exception {
        // Mock service to throw exception
        when(publicService.getPostById(eq(-1)))
                .thenThrow(new IllegalArgumentException("게시글 ID는 양수여야 합니다"));

        // Perform request and verify
        mockMvc.perform(get("/posts/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("게시글 ID는 양수여야 합니다")));
    }

    @Test
    public void testGetCommentsByPostId_BadRequest() throws Exception {
        // Mock service to throw exception
        when(publicService.getCommentsById(eq(-1)))
                .thenThrow(new IllegalArgumentException("게시글 ID는 양수여야 합니다"));

        // Perform request and verify
        mockMvc.perform(get("/comments/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("게시글 ID는 양수여야 합니다")));
    }

    @Test
    public void testGetCommentsByPostId_NotFound() throws Exception {
        // Mock service to throw exception
        when(publicService.getCommentsById(eq(999)))
                .thenThrow(new NoSuchElementException("게시글을 찾을 수 없습니다"));

        // Perform request and verify
        mockMvc.perform(get("/comments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(404)))
                .andExpect(jsonPath("$.error.message", is("게시글을 찾을 수 없습니다")));
    }

    @Test
    public void testPostComment_NotFound() throws Exception {
        // Prepare test data
        CommentRequest commentRequest = CommentRequest.builder()
                .author("Test Author")
                .content("Test Comment")
                .build();

        // Mock service to throw exception
        when(publicService.createComment(eq(999), any(CommentRequest.class)))
                .thenThrow(new NoSuchElementException("게시글을 찾을 수 없습니다"));

        // Perform request and verify
        mockMvc.perform(post("/comments/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(404)))
                .andExpect(jsonPath("$.error.message", is("게시글을 찾을 수 없습니다")));
    }

    @Test
    public void testGetCategories_ServerError() throws Exception {
        // Mock service to throw runtime exception
        when(publicService.getCategories())
                .thenThrow(new RuntimeException("서버 내부 오류"));

        // Perform request and verify
        mockMvc.perform(get("/categories"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(500)));
    }
}
