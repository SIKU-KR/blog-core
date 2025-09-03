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
import park.bumsiku.domain.entity.Category;
import park.bumsiku.domain.entity.Comment;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.repository.CategoryRepository;
import park.bumsiku.repository.CommentRepository;
import park.bumsiku.repository.PostRepository;
import park.bumsiku.utils.integration.DiscordWebhookCreator;
import park.bumsiku.utils.sorting.SortCriteria;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Mock
    private park.bumsiku.utils.sorting.PostSortBuilder postSortBuilder;

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
                .views(5L)
                .build();
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
        List<Post> postList = List.of(postMockData());
        SortCriteria sortCriteria = new SortCriteria("createdAt", "ASC", "ORDER BY p.createdAt ASC");
        when(postSortBuilder.buildSortCriteria("asc")).thenReturn(sortCriteria);
        when(postRepository.findAll(0, 10, "ORDER BY p.createdAt ASC")).thenReturn(postList);
        when(postRepository.countAll()).thenReturn(1);

        // when
        var result = publicService.getPostList(0, 10, "asc");

        // then
        assertThat(result.getContent())
                .isNotNull()
                .hasSize(1)
                .extracting("title", "summary")
                .containsExactly(
                        tuple("Sample Post Title", "Sample summary of the post")
                );
        assertThat(result.getPageSize()).isNotNull().isEqualTo(10);
        assertThat(result.getPageNumber()).isNotNull().isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);
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

    @Test
    public void incrementPostViewsShouldIncreaseViewsCount() {
        // given
        Post post = postMockData();
        Long initialViews = post.getViews();
        when(postRepository.findById(post.getId())).thenReturn(post);

        // when
        publicService.incrementPostViews(post.getId());

        // then
        assertThat(post.getViews()).isEqualTo(initialViews + 1);
        verify(postRepository).update(post);
    }

    @Test
    public void throwPostNotFoundExceptionWhenRepositoryReturnsNullPostForIncrementPostViews() {
        // given
        int postId = 999;
        when(postRepository.findById(postId)).thenReturn(null);

        // then
        assertThatThrownBy(() -> publicService.incrementPostViews(postId))
                .isInstanceOf(NoSuchElementException.class);
        verify(postRepository, never()).update(any());
    }

    @Test
    public void getPostListShouldCallRepositoryWithViewsSortDesc() {
        // given
        List<Post> postList = List.of(postMockData());
        SortCriteria sortCriteria = new SortCriteria("views", "DESC", "ORDER BY p.views DESC");
        when(postSortBuilder.buildSortCriteria("views,desc")).thenReturn(sortCriteria);
        when(postRepository.findAll(0, 10, "ORDER BY p.views DESC")).thenReturn(postList);
        when(postRepository.countAll()).thenReturn(1);

        // when
        var result = publicService.getPostList(0, 10, "views,desc");

        // then
        verify(postRepository).findAll(0, 10, "ORDER BY p.views DESC");
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getViews()).isEqualTo(5L);
    }

    @Test
    public void getPostListShouldCallRepositoryWithCreatedAtSortAsc() {
        // given
        List<Post> postList = List.of(postMockData());
        SortCriteria sortCriteria = new SortCriteria("createdAt", "ASC", "ORDER BY p.createdAt ASC");
        when(postSortBuilder.buildSortCriteria("createdAt,asc")).thenReturn(sortCriteria);
        when(postRepository.findAll(0, 5, "ORDER BY p.createdAt ASC")).thenReturn(postList);
        when(postRepository.countAll()).thenReturn(1);

        // when
        var result = publicService.getPostList(0, 5, "createdAt,asc");

        // then
        verify(postRepository).findAll(0, 5, "ORDER BY p.createdAt ASC");
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    public void getPostListByCategoryShouldCallRepositoryWithViewsSort() {
        // given
        int categoryId = 1;
        List<Post> postList = List.of(postMockData());
        SortCriteria sortCriteria = new SortCriteria("views", "ASC", "ORDER BY p.views ASC");
        when(postSortBuilder.buildSortCriteria("views,asc")).thenReturn(sortCriteria);
        when(postRepository.findAllByCategoryId(categoryId, 0, 10, "ORDER BY p.views ASC")).thenReturn(postList);
        when(postRepository.countByCategoryId(categoryId)).thenReturn(1);

        // when
        var result = publicService.getPostList(categoryId, 0, 10, "views,asc");

        // then
        verify(postRepository).findAllByCategoryId(categoryId, 0, 10, "ORDER BY p.views ASC");
        assertThat(result.getContent()).hasSize(1);
    }
}