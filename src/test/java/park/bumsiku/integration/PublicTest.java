package park.bumsiku.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import park.bumsiku.domain.entity.Category;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.repository.CategoryRepository;
import park.bumsiku.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PublicTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PostRepository postRepository;

    private List<Category> categories = new ArrayList<>();
    private List<Post> posts = new ArrayList<>();

    @BeforeEach
    public void setup() {
        // Clear existing data
        categories.clear();
        posts.clear();

        // Create test data
        createTestCategories();
        createTestPosts();
    }

    private void createTestCategories() {
        Category category1 = Category.builder()
                .name("Technology")
                .orderNum(1)
                .build();
        Category category2 = Category.builder()
                .name("Travel")
                .orderNum(2)
                .build();

        categories.add(categoryRepository.insert(category1));
        categories.add(categoryRepository.insert(category2));
    }

    private void createTestPosts() {
        for (int i = 0; i < 15; i++) {
            Category category = categories.get(i % 2);
            Post post = Post.builder()
                    .title("Test Post " + (i + 1))
                    .content("This is test content for post " + (i + 1))
                    .summary("Summary of test post " + (i + 1))
                    .state("published")
                    .category(category)
                    .createdAt(LocalDateTime.now().minusDays(15 - i))
                    .updatedAt(LocalDateTime.now().minusDays(15 - i))
                    .build();
            posts.add(postRepository.insert(post));
        }
    }

    @Test
    public void testGetPostsWithDefaultPagination() throws Exception {
        mockMvc.perform(get("/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(10)))
                .andExpect(jsonPath("$.data.pageNumber", is(0)))
                .andExpect(jsonPath("$.data.pageSize", is(10)))
                .andExpect(jsonPath("$.data.totalElements", is(15)))
                .andExpect(jsonPath("$.data.content[0].title", startsWith("Test Post")));
    }

    @Test
    public void testGetPostsWithCustomPagination() throws Exception {
        mockMvc.perform(get("/posts")
                        .param("page", "1")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(5)))
                .andExpect(jsonPath("$.data.pageNumber", is(1)))
                .andExpect(jsonPath("$.data.pageSize", is(5)))
                .andExpect(jsonPath("$.data.totalElements", is(15)));
    }

    @Test
    public void testGetPostsFilteredByCategory() throws Exception {
        Integer categoryId = categories.get(0).getId();
        mockMvc.perform(get("/posts")
                        .param("category", categoryId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content[0].title", startsWith("Test Post")))
                .andExpect(jsonPath("$.data.totalElements", lessThan(15)));
    }

    @Test
    public void testGetPostsWithNegativePageNumber() throws Exception {
        mockMvc.perform(get("/posts")
                        .param("page", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("페이지 번호는 0 이상이어야 합니다")));
    }

    @Test
    public void testGetPostsWithZeroPageSize() throws Exception {
        mockMvc.perform(get("/posts")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("페이지 크기는 1 이상이어야 합니다")));
    }

    @Test
    public void testGetPostsWithInvalidCategoryId() throws Exception {
        int nonExistentCategoryId = 9999;
        mockMvc.perform(get("/posts")
                        .param("category", String.valueOf(nonExistentCategoryId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // The API doesn't return an error for non-existent categories
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(0))); // But it should return an empty list
    }

    @Test
    public void testGetPostByIdSuccess() throws Exception {
        int existingPostId = posts.get(0).getId();

        mockMvc.perform(get("/posts/{postId}", existingPostId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(existingPostId)))
                .andExpect(jsonPath("$.data.title", startsWith("Test Post")))
                .andExpect(jsonPath("$.data.content", startsWith("This is test content")));
    }

    @Test
    public void testGetPostByIdNotFound() throws Exception {
        int nonExistentPostId = 9999;

        mockMvc.perform(get("/posts/{postId}", nonExistentPostId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(404)))
                .andExpect(jsonPath("$.error.message", containsString("Post not found")));
    }

    @Test
    public void testGetPostByIdInvalid() throws Exception {
        int invalidPostId = 0;

        mockMvc.perform(get("/posts/{postId}", invalidPostId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("게시글 ID는 1 이상이어야 합니다")));
    }
}
