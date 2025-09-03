package park.bumsiku.integration;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import park.bumsiku.config.AbstractTestSupport;
import park.bumsiku.domain.entity.Category;
import park.bumsiku.domain.entity.Comment;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.repository.CategoryRepository;
import park.bumsiku.repository.CommentRepository;
import park.bumsiku.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class PublicTest extends AbstractTestSupport {

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
        for (int i = 0; i < 15; i++) {
            Category category = categories.get(i % 2);
            // 조회수를 다양하게 설정 (인덱스가 높을수록 조회수도 높게)
            Long views = (long) ((i + 1) * 10);
            Post post = Post.builder()
                    .title("Test Post " + (i + 1))
                    .content("This is test content for post " + (i + 1))
                    .summary("Summary of test post " + (i + 1))
                    .state("published")
                    .category(category)
                    .createdAt(LocalDateTime.now().minusDays(15 - i))
                    .updatedAt(LocalDateTime.now().minusDays(15 - i))
                    .views(views)
                    .build();
            posts.add(postRepository.insert(post));
        }
    }

    private void createTestComments() {
        for (int i = 0; i < 5; i++) {
            Post post = posts.get(i);

            for (int j = 0; j < 3; j++) {
                Comment comment = Comment.builder()
                        .post(post)
                        .authorName("Commenter " + (j + 1))
                        .content("This is comment " + (j + 1) + " for post " + (i + 1))
                        .build();
                comments.add(commentRepository.insert(comment));
            }
        }

        for (int i = 10; i < 15; i++) {
            Post post = posts.get(i);
            Comment comment = Comment.builder()
                    .post(post)
                    .authorName("Single Commenter")
                    .content("This is the only comment for post " + (i + 1))
                    .build();
            comments.add(commentRepository.insert(comment));
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
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

    @Test
    public void testGetCommentsByPostIdWithMultipleComments() throws Exception {
        int postId = posts.get(0).getId();

        mockMvc.perform(get("/comments/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].authorName", startsWith("Commenter")))
                .andExpect(jsonPath("$.data[0].content", containsString("comment")))
                .andExpect(jsonPath("$.data[0].createdAt", notNullValue()));
    }

    @Test
    public void testGetCommentsByPostIdWithNoComments() throws Exception {
        int postId = posts.get(5).getId();

        mockMvc.perform(get("/comments/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    public void testGetCommentsByPostIdWithSingleComment() throws Exception {
        int postId = posts.get(10).getId();

        mockMvc.perform(get("/comments/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].authorName", is("Single Commenter")))
                .andExpect(jsonPath("$.data[0].content", containsString("only comment")))
                .andExpect(jsonPath("$.data[0].createdAt", notNullValue()));
    }

    @Test
    public void testGetCommentsByPostIdNotFound() throws Exception {
        int nonExistentPostId = 9999;

        mockMvc.perform(get("/comments/{postId}", nonExistentPostId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(404)))
                .andExpect(jsonPath("$.error.message", containsString("Post not found")));
    }

    @Test
    public void testGetCommentsByPostIdInvalid() throws Exception {
        int invalidPostId = 0;

        mockMvc.perform(get("/comments/{postId}", invalidPostId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("게시글 ID는 1 이상이어야 합니다")));
    }

    @Test
    public void testPostCommentSuccess() throws Exception {
        int postId = posts.get(0).getId();
        String content = "This is a new test comment";
        String author = "Test Author";

        mockMvc.perform(post("/comments/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                park.bumsiku.domain.dto.request.CommentRequest.builder()
                                        .content(content)
                                        .author(author)
                                        .build()
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.authorName", is(author)))
                .andExpect(jsonPath("$.data.content", is(content)))
                .andExpect(jsonPath("$.data.id", greaterThan(0)))
                .andExpect(jsonPath("$.data.createdAt", notNullValue()));
    }

    @Test
    public void testPostCommentInvalidPostId() throws Exception {
        int invalidPostId = 0;
        String content = "This is a test comment";
        String author = "Test Author";

        mockMvc.perform(post("/comments/{postId}", invalidPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                park.bumsiku.domain.dto.request.CommentRequest.builder()
                                        .content(content)
                                        .author(author)
                                        .build()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("게시글 ID는 1 이상이어야 합니다")));
    }

    @Test
    public void testPostCommentNonExistentPostId() throws Exception {
        int nonExistentPostId = 9999;
        String content = "This is a test comment";
        String author = "Test Author";

        mockMvc.perform(post("/comments/{postId}", nonExistentPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                park.bumsiku.domain.dto.request.CommentRequest.builder()
                                        .content(content)
                                        .author(author)
                                        .build()
                        )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(404)))
                .andExpect(jsonPath("$.error.message", containsString("Post not found")));
    }

    @Test
    public void testPostCommentEmptyContent() throws Exception {
        int postId = posts.get(0).getId();
        String content = "";
        String author = "Test Author";

        mockMvc.perform(post("/comments/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                park.bumsiku.domain.dto.request.CommentRequest.builder()
                                        .content(content)
                                        .author(author)
                                        .build()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("댓글 내용을 입력해주세요")));
    }

    @Test
    public void testPostCommentContentTooLong() throws Exception {
        int postId = posts.get(0).getId();
        // Create a string longer than 500 characters
        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            contentBuilder.append("0123456789");
        }
        String content = contentBuilder.toString(); // 510 characters
        String author = "Test Author";

        mockMvc.perform(post("/comments/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                park.bumsiku.domain.dto.request.CommentRequest.builder()
                                        .content(content)
                                        .author(author)
                                        .build()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("댓글은 1자 이상 500자 이하로 입력해주세요")));
    }

    @Test
    public void testPostCommentEmptyAuthor() throws Exception {
        int postId = posts.get(0).getId();
        String content = "This is a test comment";
        String author = "";

        mockMvc.perform(post("/comments/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                park.bumsiku.domain.dto.request.CommentRequest.builder()
                                        .content(content)
                                        .author(author)
                                        .build()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("작성자 이름을 입력해주세요")));
    }

    @Test
    public void testPostCommentAuthorTooShort() throws Exception {
        int postId = posts.get(0).getId();
        String content = "This is a test comment";
        String author = "A"; // Only 1 character, minimum is 2

        mockMvc.perform(post("/comments/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                park.bumsiku.domain.dto.request.CommentRequest.builder()
                                        .content(content)
                                        .author(author)
                                        .build()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("작성자 이름은 2자 이상 20자 이하로 입력해주세요")));
    }

    @Test
    public void testPostCommentAuthorTooLong() throws Exception {
        int postId = posts.get(0).getId();
        String content = "This is a test comment";
        String author = "ThisAuthorNameIsTooLongForTheSystem"; // More than 20 characters

        mockMvc.perform(post("/comments/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                park.bumsiku.domain.dto.request.CommentRequest.builder()
                                        .content(content)
                                        .author(author)
                                        .build()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("작성자 이름은 2자 이상 20자 이하로 입력해주세요")));
    }

    @Test
    public void testPostCommentNullRequest() throws Exception {
        int postId = posts.get(0).getId();

        mockMvc.perform(post("/comments/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetCategoriesSuccess() throws Exception {
        mockMvc.perform(get("/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id", is(categories.get(0).getId())))
                .andExpect(jsonPath("$.data[0].name", is("Technology")))
                .andExpect(jsonPath("$.data[0].order", is(1)))
                .andExpect(jsonPath("$.data[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$.data[0].postCount", is(8)))
                .andExpect(jsonPath("$.data[1].id", is(categories.get(1).getId())))
                .andExpect(jsonPath("$.data[1].name", is("Travel")))
                .andExpect(jsonPath("$.data[1].order", is(2)))
                .andExpect(jsonPath("$.data[1].createdAt", notNullValue()))
                .andExpect(jsonPath("$.data[1].postCount", is(7)));
    }

    @Test
    public void testIncrementPostViewsSuccess() throws Exception {
        int postId = posts.get(0).getId();
        Post initialPost = postRepository.findById(postId);
        Long initialViews = initialPost.getViews();

        mockMvc.perform(patch("/posts/{postId}/views", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data").doesNotExist());

        Post updatedPost = postRepository.findById(postId);
        assertThat(updatedPost.getViews()).isEqualTo(initialViews + 1);
    }

    @Test
    public void testIncrementPostViewsNotFound() throws Exception {
        int nonExistentPostId = 9999;

        mockMvc.perform(patch("/posts/{postId}/views", nonExistentPostId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(404)))
                .andExpect(jsonPath("$.error.message", containsString("Post not found")));
    }

    @Test
    public void testIncrementPostViewsInvalidId() throws Exception {
        int invalidPostId = 0;

        mockMvc.perform(patch("/posts/{postId}/views", invalidPostId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", containsString("게시글 ID는 1 이상이어야 합니다")));
    }

    @Test
    public void testIncrementPostViewsMultipleTimes() throws Exception {
        int postId = posts.get(1).getId();
        Post initialPost = postRepository.findById(postId);
        Long initialViews = initialPost.getViews();
        int incrementCount = 5;

        for (int i = 0; i < incrementCount; i++) {
            mockMvc.perform(patch("/posts/{postId}/views", postId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)));
        }

        Post updatedPost = postRepository.findById(postId);
        assertThat(updatedPost.getViews()).isEqualTo(initialViews + incrementCount);
    }

    @Test
    public void testGetPostsSortedByViewsDesc() throws Exception {
        mockMvc.perform(get("/posts")
                        .param("sort", "views,desc")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(5)))
                .andExpect(jsonPath("$.data.content[0].views", is(150))) // 15 * 10
                .andExpect(jsonPath("$.data.content[1].views", is(140))) // 14 * 10
                .andExpect(jsonPath("$.data.content[4].views", is(110))); // 11 * 10
    }

    @Test
    public void testGetPostsSortedByViewsAsc() throws Exception {
        mockMvc.perform(get("/posts")
                        .param("sort", "views,asc")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(5)))
                .andExpect(jsonPath("$.data.content[0].views", is(10))) // 1 * 10
                .andExpect(jsonPath("$.data.content[1].views", is(20))) // 2 * 10
                .andExpect(jsonPath("$.data.content[4].views", is(50))); // 5 * 10
    }

    @Test
    public void testGetPostsSortedByCreatedAtAsc() throws Exception {
        mockMvc.perform(get("/posts")
                        .param("sort", "createdAt,asc")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.content[0].title", is("Test Post 1"))) // 가장 오래된 게시글
                .andExpect(jsonPath("$.data.content[1].title", is("Test Post 2")))
                .andExpect(jsonPath("$.data.content[2].title", is("Test Post 3")));
    }

    @Test
    public void testGetPostsSortedByCreatedAtDesc() throws Exception {
        mockMvc.perform(get("/posts")
                        .param("sort", "createdAt,desc")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.content[0].title", is("Test Post 15"))) // 가장 최근 게시글
                .andExpect(jsonPath("$.data.content[1].title", is("Test Post 14")))
                .andExpect(jsonPath("$.data.content[2].title", is("Test Post 13")));
    }

    @Test
    public void testGetPostsByCategoryWithViewsSort() throws Exception {
        Integer categoryId = categories.get(0).getId();
        mockMvc.perform(get("/posts")
                        .param("category", categoryId.toString())
                        .param("sort", "views,desc")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
        ;
    }

    @Test
    public void testGetPostsWithInvalidSort() throws Exception {
        mockMvc.perform(get("/posts")
                        .param("sort", "invalidField,desc")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(5)))
                .andExpect(jsonPath("$.data.content[0].title", is("Test Post 15"))); // 기본값 createdAt,desc
    }

    @Test
    public void testGetPostsWithInvalidDirection() throws Exception {
        mockMvc.perform(get("/posts")
                        .param("sort", "views,invalid")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(5)))
                .andExpect(jsonPath("$.data.content[0].views", is(150))); // DESC가 기본값이므로 가장 높은 조회수
    }
}
