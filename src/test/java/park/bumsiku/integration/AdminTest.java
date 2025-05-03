package park.bumsiku.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import park.bumsiku.domain.dto.request.CreatePostRequest;
import park.bumsiku.domain.entity.Category;
import park.bumsiku.domain.entity.Comment;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.repository.CategoryRepository;
import park.bumsiku.repository.CommentRepository;
import park.bumsiku.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private final List<Category> categories = new ArrayList<>();
    private final List<Post> posts = new ArrayList<>();
    private final List<Comment> comments = new ArrayList<>();

    @BeforeEach
    public void setup() {
        // Clear existing data
        categories.clear();
        posts.clear();
        comments.clear();

        // Create test data
        createTestCategories();
        createTestPosts();
        createTestComments();

        // flush
        entityManager.flush();
    }

    private void createTestCategories() {
        Category category1 = Category.builder()
                .name("Technology")
                .ordernum(1)
                .build();
        Category category2 = Category.builder()
                .name("Travel")
                .ordernum(2)
                .build();

        categories.add(categoryRepository.insert(category1));
        categories.add(categoryRepository.insert(category2));
    }

    private void createTestPosts() {
        for (int i = 0; i < 5; i++) {
            Category category = categories.get(i % 2);
            Post post = Post.builder()
                    .title("Test Post " + (i + 1))
                    .content("This is test content for post " + (i + 1))
                    .summary("Summary of test post " + (i + 1))
                    .state("published")
                    .category(category)
                    .createdAt(LocalDateTime.now().minusDays(5 - i))
                    .updatedAt(LocalDateTime.now().minusDays(5 - i))
                    .build();
            posts.add(postRepository.insert(post));
        }
    }

    private void createTestComments() {
        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            // Create 2 comments for each post
            for (int j = 0; j < 2; j++) {
                Comment comment = Comment.builder()
                        .post(post)
                        .authorName("Test Author " + (j + 1))
                        .content("This is a test comment " + (j + 1) + " for post " + (i + 1))
                        .build();
                comments.add(commentRepository.insert(comment));
            }
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddPost_Success() throws Exception {
        // Prepare test data
        CreatePostRequest request = CreatePostRequest.builder()
                .title("New Test Post")
                .content("This is content for the new test post")
                .summary("Summary of the new test post")
                .category(categoryRepository.findAll().get(0).getId())
                .build();

        // Perform request and verify
        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", greaterThan(0)))
                .andExpect(jsonPath("$.data.title", is("New Test Post")))
                .andExpect(jsonPath("$.data.content", is("This is content for the new test post")))
                .andExpect(jsonPath("$.data.createdAt", notNullValue()))
                .andExpect(jsonPath("$.data.updatedAt", notNullValue()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddPost_EmptyTitle() throws Exception {
        // Prepare test data with empty title
        CreatePostRequest request = CreatePostRequest.builder()
                .title("")
                .content("This is content for the new test post")
                .summary("Summary of the new test post")
                .category(categories.get(0).getId())
                .build();

        // Perform request and verify
        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("제목을 입력해주세요")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddPost_TitleTooLong() throws Exception {
        // Create a title longer than 100 characters
        StringBuilder titleBuilder = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            titleBuilder.append("0123456789");
        }
        String longTitle = titleBuilder.toString(); // 110 characters

        // Prepare test data with too long title
        CreatePostRequest request = CreatePostRequest.builder()
                .title(longTitle)
                .content("This is content for the new test post")
                .summary("Summary of the new test post")
                .category(categories.get(0).getId())
                .build();

        // Perform request and verify
        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("제목은 1자 이상 100자 이하로 입력해주세요")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddPost_EmptyContent() throws Exception {
        // Prepare test data with empty content
        CreatePostRequest request = CreatePostRequest.builder()
                .title("New Test Post")
                .content("")
                .summary("Summary of the new test post")
                .category(categories.get(0).getId())
                .build();

        // Perform request and verify
        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("내용을 입력해주세요")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddPost_ContentTooLong() throws Exception {
        // Create content longer than 10000 characters
        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 0; i < 1001; i++) {
            contentBuilder.append("0123456789");
        }
        String longContent = contentBuilder.toString(); // 10010 characters

        // Prepare test data with too long content
        CreatePostRequest request = CreatePostRequest.builder()
                .title("New Test Post")
                .content(longContent)
                .summary("Summary of the new test post")
                .category(categories.get(0).getId())
                .build();

        // Perform request and verify
        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("내용은 1자 이상 10000자 이하로 입력해주세요")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddPost_EmptySummary() throws Exception {
        // Prepare test data with empty summary
        CreatePostRequest request = CreatePostRequest.builder()
                .title("New Test Post")
                .content("This is content for the new test post")
                .summary("")
                .category(categories.get(0).getId())
                .build();

        // Perform request and verify
        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("요약을 입력해주세요")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddPost_SummaryTooLong() throws Exception {
        // Create summary longer than 200 characters
        StringBuilder summaryBuilder = new StringBuilder();
        for (int i = 0; i < 21; i++) {
            summaryBuilder.append("0123456789");
        }
        String longSummary = summaryBuilder.toString(); // 210 characters

        // Prepare test data with too long summary
        CreatePostRequest request = CreatePostRequest.builder()
                .title("New Test Post")
                .content("This is content for the new test post")
                .summary(longSummary)
                .category(categories.get(0).getId())
                .build();

        // Perform request and verify
        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("요약은 1자 이상 200자 이하로 입력해주세요")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddPost_EmptyCategory() throws Exception {
        // Prepare test data with empty category
        CreatePostRequest request = CreatePostRequest.builder()
                .title("New Test Post")
                .content("This is content for the new test post")
                .summary("Summary of the new test post")
                .build();

        // Perform request and verify
        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("카테고리를 선택해주세요")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAddPost_InvalidCategory() throws Exception {
        // Prepare test data with invalid category
        CreatePostRequest request = CreatePostRequest.builder()
                .title("New Test Post")
                .content("This is content for the new test post")
                .summary("Summary of the new test post")
                .category(9999) // Non-existent category ID
                .build();

        // Perform request and verify
        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)));
    }

    @Test
    public void testAddPost_Unauthorized() throws Exception {
        // Prepare test data
        CreatePostRequest request = CreatePostRequest.builder()
                .title("New Test Post")
                .content("This is content for the new test post")
                .summary("Summary of the new test post")
                .category(categories.get(0).getId())
                .build();

        // Perform request without authentication and verify
        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteComment_Success() throws Exception {
        // Get a valid comment ID
        Long commentId = comments.get(0).getId();

        // Perform request and verify
        mockMvc.perform(delete("/admin/comments/" + commentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.message", is("Comment deleted successfully")));

        // Verify the comment was actually deleted
        Comment deletedComment = commentRepository.findById(commentId);
        assert deletedComment == null;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteComment_InvalidId_NonNumeric() throws Exception {
        // Perform request with non-numeric ID and verify
        mockMvc.perform(delete("/admin/comments/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("댓글 ID는 숫자여야 합니다")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteComment_InvalidId_Zero() throws Exception {
        // Perform request with zero ID and verify
        mockMvc.perform(delete("/admin/comments/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("댓글 ID는 1 이상이어야 합니다")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteComment_InvalidId_Negative() throws Exception {
        // Perform request with negative ID and verify
        mockMvc.perform(delete("/admin/comments/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("댓글 ID는 1 이상이어야 합니다")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteComment_NonExistentId() throws Exception {
        // Use a non-existent comment ID (assuming IDs are sequential)
        Long nonExistentId = comments.get(comments.size() - 1).getId() + 1000;

        // Perform request and verify
        mockMvc.perform(delete("/admin/comments/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(404)))
                .andExpect(jsonPath("$.error.message", containsString("Comment not found")));
    }

    @Test
    public void testDeleteComment_Unauthorized() throws Exception {
        // Get a valid comment ID
        Long commentId = comments.get(0).getId();

        // Perform request without authentication and verify
        mockMvc.perform(delete("/admin/comments/" + commentId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeletePost_Success() throws Exception {
        // Get a valid post ID
        int postId = posts.get(0).getId();

        // Perform request and verify
        mockMvc.perform(delete("/admin/posts/" + postId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.message", is("Post deleted successfully")));

        // Verify the post was actually deleted
        Post deletedPost = postRepository.findById(postId);
        assert deletedPost == null;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeletePost_InvalidId_Zero() throws Exception {
        // Perform request with zero ID and verify
        mockMvc.perform(delete("/admin/posts/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("게시글 ID는 1 이상이어야 합니다")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeletePost_InvalidId_Negative() throws Exception {
        // Perform request with negative ID and verify
        mockMvc.perform(delete("/admin/posts/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("게시글 ID는 1 이상이어야 합니다")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeletePost_InvalidId_NonNumeric() throws Exception {
        // Perform request with non-numeric ID and verify
        mockMvc.perform(delete("/admin/posts/abc"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeletePost_NonExistentId() throws Exception {
        // Use a non-existent post ID (assuming IDs are sequential)
        int nonExistentId = posts.get(posts.size() - 1).getId() + 1000;

        // Perform request and verify
        mockMvc.perform(delete("/admin/posts/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(404)))
                .andExpect(jsonPath("$.error.message", containsString("Post not found")));
    }

    @Test
    public void testDeletePost_Unauthorized() throws Exception {
        // Get a valid post ID
        int postId = posts.get(0).getId();

        // Perform request without authentication and verify
        mockMvc.perform(delete("/admin/posts/" + postId))
                .andExpect(status().isUnauthorized());
    }
}
