package park.bumsiku.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import park.bumsiku.config.ClockConfig;
import park.bumsiku.config.LoggingConfig;
import park.bumsiku.config.SecurityConfig;
import park.bumsiku.domain.dto.request.CreateCategoryRequest;
import park.bumsiku.domain.dto.request.CreatePostRequest;
import park.bumsiku.domain.dto.request.UpdateCategoryRequest;
import park.bumsiku.domain.dto.request.UpdatePostRequest;
import park.bumsiku.domain.dto.response.CategoryResponse;
import park.bumsiku.domain.dto.response.PostResponse;
import park.bumsiku.domain.dto.response.UploadImageResponse;
import park.bumsiku.service.PrivateService;
import park.bumsiku.utils.ArgumentValidator;
import park.bumsiku.utils.DiscordWebhookCreator;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, ClockConfig.class, LoggingConfig.class})
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrivateService privateService;

    @MockitoBean
    private ArgumentValidator validator;

    @MockitoBean
    private DiscordWebhookCreator webhookCreator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCreateCategory_Success() throws Exception {
        // Prepare test data
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("New Category")
                .orderNum(1)
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(1)
                .name("New Category")
                .order(1)
                .createdAt(LocalDateTime.now())
                .build();

        // Mock service response
        when(privateService.createCategory(any(CreateCategoryRequest.class))).thenReturn(response);

        // Perform request and verify
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("New Category")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUpdateCategory_Success() throws Exception {
        // Prepare test data
        Integer categoryId = 1;
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Updated Category")
                .orderNum(1)
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(1)
                .name("Updated Category")
                .order(1)
                .createdAt(LocalDateTime.now())
                .build();

        // Mock service response
        when(privateService.updateCategory(eq(categoryId), any(UpdateCategoryRequest.class))).thenReturn(response);

        // Perform request and verify
        mockMvc.perform(put("/admin/categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("Updated Category")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteComment_Success() throws Exception {
        // Mock service response - void method
        doNothing().when(privateService).deleteComment("1");

        // Perform request and verify
        mockMvc.perform(delete("/admin/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddImage_Success() throws Exception {
        // Prepare test data
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        UploadImageResponse response = UploadImageResponse.builder()
                .url("/images/test-image.jpg")
                .build();

        // Mock service response
        when(privateService.uploadImage(any())).thenReturn(response);

        // Perform request and verify
        mockMvc.perform(multipart("/admin/images")
                        .file(imageFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.url", is("/images/test-image.jpg")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddPost_Success() throws Exception {
        // Prepare test data
        CreatePostRequest request = CreatePostRequest.builder()
                .title("New Post")
                .content("Post Content")
                .summary("Post Summary")
                .category(1)
                .build();

        PostResponse response = PostResponse.builder()
                .id(1)
                .title("New Post")
                .content("Post Content")
                .createdAt("2023-01-01T12:00:00")
                .updatedAt("2023-01-01T12:00:00")
                .build();

        // Mock service response
        when(privateService.createPost(any(CreatePostRequest.class))).thenReturn(response);

        // Perform request and verify
        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.title", is("New Post")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeletePost_Success() throws Exception {
        // Mock service response - void method
        doNothing().when(privateService).deletePost(1);

        // Perform request and verify
        mockMvc.perform(delete("/admin/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testEditPost_Success() throws Exception {
        // Prepare test data
        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("Updated Post")
                .content("Updated Content")
                .summary("Updated Summary")
                .category(1)
                .build();

        PostResponse response = PostResponse.builder()
                .id(1)
                .title("Updated Post")
                .content("Updated Content")
                .createdAt("2023-01-01T12:00:00")
                .updatedAt("2023-01-01T12:00:00")
                .build();

        // Mock service response
        when(privateService.updatePost(eq(1), any(UpdatePostRequest.class))).thenReturn(response);

        // Perform request and verify
        mockMvc.perform(put("/admin/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.title", is("Updated Post")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testEditPost_NotFound() throws Exception {
        // Prepare test data
        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("Updated Post")
                .content("Updated Content")
                .summary("Updated Summary")
                .category(1)
                .build();

        // Mock service to throw exception
        when(privateService.updatePost(eq(999), any(UpdatePostRequest.class)))
                .thenThrow(new NoSuchElementException("게시글을 찾을 수 없습니다"));

        // Perform request and verify
        mockMvc.perform(put("/admin/posts/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(404)))
                .andExpect(jsonPath("$.error.message", is("게시글을 찾을 수 없습니다")));
    }

    @Test
    public void testAdminEndpoint_Unauthorized() throws Exception {
        // Test without authentication
        mockMvc.perform(get("/admin/posts/1"))
                .andExpect(status().isUnauthorized());
    }

    // Tests for 400 Bad Request status codes

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUpdateCategory_BadRequest() throws Exception {
        // Prepare invalid test data
        Integer categoryId = 1;
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("") // Empty name is invalid
                .orderNum(1)
                .build();

        // Mock validator to throw exception
        doThrow(new IllegalArgumentException("카테고리 이름은 필수입니다"))
                .when(privateService).updateCategory(eq(categoryId), any(UpdateCategoryRequest.class));

        // Perform request and verify
        mockMvc.perform(put("/admin/categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("카테고리 이름은 필수입니다")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteComment_BadRequest() throws Exception {
        // Mock service to throw exception
        doThrow(new IllegalArgumentException("유효하지 않은 댓글 ID입니다"))
                .when(privateService).deleteComment("invalid");

        // Perform request and verify
        mockMvc.perform(delete("/admin/comments/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("유효하지 않은 댓글 ID입니다")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddImage_BadRequest() throws Exception {
        // Prepare empty file
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        // Mock service to throw exception
        when(privateService.uploadImage(any()))
                .thenThrow(new IllegalArgumentException("이미지 파일이 필요합니다"));

        // Perform request and verify
        mockMvc.perform(multipart("/admin/images")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("이미지 파일이 필요합니다")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddPost_BadRequest() throws Exception {
        // Prepare invalid test data
        CreatePostRequest request = CreatePostRequest.builder()
                .title("")  // Empty title is invalid
                .content("Post Content")
                .summary("Post Summary")
                .category(1)
                .build();

        // Mock service to throw exception
        when(privateService.createPost(any(CreatePostRequest.class)))
                .thenThrow(new IllegalArgumentException("제목은 필수입니다"));

        // Perform request and verify
        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("제목은 필수입니다")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeletePost_BadRequest() throws Exception {
        // Mock service to throw exception
        doThrow(new IllegalArgumentException("유효하지 않은 게시글 ID입니다"))
                .when(privateService).deletePost(-1);

        // Perform request and verify
        mockMvc.perform(delete("/admin/posts/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("유효하지 않은 게시글 ID입니다")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testEditPost_BadRequest() throws Exception {
        // Prepare invalid test data
        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("")  // Empty title is invalid
                .content("Updated Content")
                .summary("Updated Summary")
                .category(1)
                .build();

        // Mock service to throw exception
        when(privateService.updatePost(eq(1), any(UpdatePostRequest.class)))
                .thenThrow(new IllegalArgumentException("제목은 필수입니다"));

        // Perform request and verify
        mockMvc.perform(put("/admin/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("제목은 필수입니다")));
    }

    // Tests for 404 Not Found status codes

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteComment_NotFound() throws Exception {
        // Mock service to throw exception
        doThrow(new NoSuchElementException("댓글을 찾을 수 없습니다"))
                .when(privateService).deleteComment("999");

        // Perform request and verify
        mockMvc.perform(delete("/admin/comments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(404)))
                .andExpect(jsonPath("$.error.message", is("댓글을 찾을 수 없습니다")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeletePost_NotFound() throws Exception {
        // Mock service to throw exception
        doThrow(new NoSuchElementException("게시글을 찾을 수 없습니다"))
                .when(privateService).deletePost(999);

        // Perform request and verify
        mockMvc.perform(delete("/admin/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(404)))
                .andExpect(jsonPath("$.error.message", is("게시글을 찾을 수 없습니다")));
    }

    // Tests for 500 Server Error status codes

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUpdateCategory_ServerError() throws Exception {
        // Prepare test data
        Integer categoryId = 1;
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Updated Category")
                .orderNum(1)
                .build();

        // Mock service to throw runtime exception
        when(privateService.updateCategory(eq(categoryId), any(UpdateCategoryRequest.class)))
                .thenThrow(new RuntimeException("서버 내부 오류"));

        // Perform request and verify
        mockMvc.perform(put("/admin/categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(500)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteComment_ServerError() throws Exception {
        // Mock service to throw runtime exception
        doThrow(new RuntimeException("서버 내부 오류"))
                .when(privateService).deleteComment("1");

        // Perform request and verify
        mockMvc.perform(delete("/admin/comments/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(500)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddImage_ServerError() throws Exception {
        // Prepare test data
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Mock service to throw runtime exception
        when(privateService.uploadImage(any()))
                .thenThrow(new RuntimeException("서버 내부 오류"));

        // Perform request and verify
        mockMvc.perform(multipart("/admin/images")
                        .file(imageFile))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(500)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddPost_ServerError() throws Exception {
        // Prepare test data
        CreatePostRequest request = CreatePostRequest.builder()
                .title("New Post")
                .content("Post Content")
                .summary("Post Summary")
                .category(1)
                .build();

        // Mock service to throw runtime exception
        when(privateService.createPost(any(CreatePostRequest.class)))
                .thenThrow(new RuntimeException("서버 내부 오류"));

        // Perform request and verify
        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(500)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeletePost_ServerError() throws Exception {
        // Mock service to throw runtime exception
        doThrow(new RuntimeException("서버 내부 오류"))
                .when(privateService).deletePost(1);

        // Perform request and verify
        mockMvc.perform(delete("/admin/posts/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(500)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testEditPost_ServerError() throws Exception {
        // Prepare test data
        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("Updated Post")
                .content("Updated Content")
                .summary("Updated Summary")
                .category(1)
                .build();

        // Mock service to throw runtime exception
        when(privateService.updatePost(eq(1), any(UpdatePostRequest.class)))
                .thenThrow(new RuntimeException("서버 내부 오류"));

        // Perform request and verify
        mockMvc.perform(put("/admin/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(500)));
    }
}
