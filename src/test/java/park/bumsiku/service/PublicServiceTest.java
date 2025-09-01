package park.bumsiku.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import park.bumsiku.domain.dto.request.CommentRequest;
import park.bumsiku.domain.dto.response.CategoryResponse;
import park.bumsiku.domain.dto.response.CommentResponse;
import park.bumsiku.domain.dto.response.PostResponse;
import park.bumsiku.domain.dto.response.PostSummaryResponse;
import park.bumsiku.domain.entity.Category;
import park.bumsiku.domain.entity.Comment;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.repository.CategoryRepository;
import park.bumsiku.repository.CommentRepository;
import park.bumsiku.repository.PostRepository;
import park.bumsiku.utils.integration.DiscordWebhookCreator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PublicServiceTest {

    @InjectMocks
    private PublicService publicService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DiscordWebhookCreator discord;

    private Post postMockData() {
        Category mockCategory = Category.builder()
                .id(1)
                .name("Technology")
                .ordernum(1)
                .build();
        return Post.builder()
                .id(1)
                .title("Sample Post Title")
                .content("This is a sample content for the post. Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                .summary("Sample summary of the post")
                .state("PUBLISHED")
                .category(mockCategory)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private List<PostSummaryResponse> postSummaryMockData() {
        return List.of(
                PostSummaryResponse.builder()
                        .id(1)
                        .title("title1")
                        .summary("summary1")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                PostSummaryResponse.builder()
                        .id(2)
                        .title("title2")
                        .summary("summary2")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
    }

    private List<Comment> commentMockData() {
        Post post = postMockData();
        return List.of(
                Comment.builder().id(1L).post(post).authorName("Alice").content("정말 좋은 포스트네요!").createdAt(LocalDateTime.now()).build(),
                Comment.builder().id(2L).post(post).authorName("Bob").content("유익한 정보 감사합니다.").createdAt(LocalDateTime.now()).build(),
                Comment.builder().id(3L).post(post).authorName("Charlie").content("더 많은 글 기대할게요.").createdAt(LocalDateTime.now()).build()
        );
    }

    private CommentRequest commentRequestMockData() {
        return CommentRequest.builder()
                .author("peter")
                .content("content of mock comment request")
                .build();
    }

    @Test
    public void returnPostSummaryListResponseWithMockedData() {
        // given
        List<PostSummaryResponse> postList = postSummaryMockData();
        when(postRepository.findAll(0, 10)).thenReturn(postList);

        // when
        var result = publicService.getPostList(0, 10, "asc");

        // then
        assertThat(result.getContent())
                .isNotNull()
                .hasSize(2)
                .extracting("title", "summary")
                .containsExactly(
                        tuple("title1", "summary1"),
                        tuple("title2", "summary2")
                );
        assertThat(result.getPageSize()).isNotNull().isEqualTo(10);
        assertThat(result.getPageNumber()).isNotNull().isEqualTo(0);
    }

    @Test
    public void createAndReturnPostResponseObjectFromMockedPost() {
        // given
        Post mockPost = postMockData();
        int postId = mockPost.getId();

        when(postRepository.findById(postId)).thenReturn(mockPost);

        // when
        PostResponse postResponse = publicService.getPostById(postId);

        // then
        assertThat(postResponse)
                .isNotNull()
                .extracting("id", "title", "content")
                .containsExactly(
                        mockPost.getId(),
                        mockPost.getTitle(),
                        mockPost.getContent()
                );
    }

    @Test
    public void throwPostNotFoundExceptionWhenRepositoryReturnsNullPostForGetPostById() {
        // given
        int postId = 111;
        when(postRepository.findById(postId)).thenReturn(null);

        // then
        assertThatThrownBy(() -> publicService.getPostById(postId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void createAndReturnListOfCommentResponse() {
        // given
        List<Comment> commentList = commentMockData();
        Post post = postMockData();

        when(postRepository.findById(post.getId())).thenReturn(post);
        when(commentRepository.findAllByPost(post)).thenReturn(commentList);

        // when
        List<CommentResponse> result = publicService.getCommentsById(post.getId());

        // then
        assertThat(result)
                .isNotNull()
                .hasSize(3)
                .extracting("authorName", "content")
                .containsExactly(
                        tuple(commentList.get(0).getAuthorName(), commentList.get(0).getContent()),
                        tuple(commentList.get(1).getAuthorName(), commentList.get(1).getContent()),
                        tuple(commentList.get(2).getAuthorName(), commentList.get(2).getContent())
                );
    }

    @Test
    public void throwPostNotFoundExceptionWhenRepositoryReturnsNullPostForGetCommentsById() {
        // given
        int postId = 111;
        when(postRepository.findById(postId)).thenReturn(null);

        // then
        assertThatThrownBy(() -> publicService.getCommentsById(postId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void createAndReturnCommentResponse() {
        // given
        CommentRequest commentRequest = commentRequestMockData();
        Post post = postMockData();

        when(postRepository.findById(post.getId())).thenReturn(post);
        when(commentRepository.insert(any(Comment.class)))
                .thenReturn(Comment.builder()
                        .id(1L)
                        .post(post)
                        .authorName(commentRequest.getAuthor())
                        .content(commentRequest.getContent())
                        .createdAt(LocalDateTime.now())
                        .build()
                );

        // when
        CommentResponse result = publicService.createComment(post.getId(), commentRequest);

        // then
        assertThat(result)
                .isNotNull()
                .extracting("authorName", "content")
                .containsExactly(
                        commentRequest.getAuthor(),
                        commentRequest.getContent()
                );
    }

    @Test
    public void throwPostNotFoundExceptionWhenRepositoryReturnsNullPostForCreateComment() {
        // given
        int postId = 111;
        CommentRequest commentRequest = commentRequestMockData();
        when(postRepository.findById(postId)).thenReturn(null);

        // then
        assertThatThrownBy(() -> publicService.createComment(postId, commentRequest))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void getCategoriesShouldReturnOrderedListOfCategoryResponse() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Category category1 = Category.builder().id(1).name("Tech").ordernum(1).createdAt(now).build();
        Category category2 = Category.builder().id(2).name("Life").ordernum(2).createdAt(now).build();
        List<Category> mockCategories = List.of(category1, category2);

        when(categoryRepository.findAll()).thenReturn(mockCategories);

        // when
        List<CategoryResponse> result = publicService.getCategories();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("id", "name", "order")
                .containsExactly(
                        tuple(category1.getId(), category1.getName(), category1.getOrdernum()),
                        tuple(category2.getId(), category2.getName(), category2.getOrdernum())
                );
    }
}