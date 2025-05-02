package park.bumsiku.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import park.bumsiku.config.SecurityConfig;
import park.bumsiku.domain.dto.*;
import park.bumsiku.exception.PostNotFoundException;
import park.bumsiku.service.PrivateService;
import park.bumsiku.validator.ArgumentValidator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrivateService privateService;

    @MockBean
    private ArgumentValidator validator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPutCategory_Success() throws Exception {
        // Prepare test data
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .id(1)
                .category("Updated Category")
                .order(1)
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(1)
                .name("Updated Category")
                .orderNum(1)
                .createdAt(LocalDateTime.now())
                .build();

        // Mock service response
        when(privateService.updateCategory(any(UpdateCategoryRequest.class))).thenReturn(response);

        // Perform request and verify
        mockMvc.perform(put("/admin/categories")
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
                .category("Test Category")
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
                .category("Updated Category")
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
                .category("Updated Category")
                .build();

        // Mock service to throw exception
        when(privateService.updatePost(eq(999), any(UpdatePostRequest.class)))
                .thenThrow(new PostNotFoundException("게시글을 찾을 수 없습니다"));

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
}
