package park.bumsiku.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import park.bumsiku.domain.dto.response.CommentResponse;
import park.bumsiku.domain.dto.response.PostResponse;
import park.bumsiku.domain.entity.Comment;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.repository.CommentRepository;
import park.bumsiku.repository.PostRepository;
import park.bumsiku.repository.TagRepository;
import park.bumsiku.utils.integration.DiscordWebhookCreator;
import park.bumsiku.utils.sorting.SortCriteria;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static park.bumsiku.support.TestFixtures.*;

@ExtendWith(MockitoExtension.class)
public class PublicServiceTest {

    @InjectMocks
    private PublicService publicService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TagRepository tagRepository;


    @Mock
    private DiscordWebhookCreator discord;

    @Mock
    private park.bumsiku.utils.sorting.PostSortBuilder postSortBuilder;

    @Test
    public void returnPostSummaryListResponseWithMockedData() {
        // given
        Post mockPost = buildPost(builder -> builder
                .slug("sample-post-title")
                .title("Sample Post Title")
                .summary("Sample summary of the post")
                .content("This is a sample content for the post. Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                .views(5L));
        List<Post> postList = List.of(mockPost);
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
                        tuple(mockPost.getTitle(), mockPost.getSummary())
                );
        assertThat(result.getPageSize()).isNotNull().isEqualTo(10);
        assertThat(result.getPageNumber()).isNotNull().isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void createAndReturnPostResponseObjectFromMockedPostSlug() {
        // given
        Post mockPost = buildPost(builder -> builder
                .slug("sample-post-title")
                .title("Sample Post Title")
                .content("This is a sample content for the post. Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                .summary("Sample summary of the post")
                .views(5L));

        when(postRepository.findBySlug("sample-post-title")).thenReturn(mockPost);

        // when
        PostResponse postResponse = publicService.getPostBySlug("sample-post-title");

        // then
        assertThat(postResponse)
                .isNotNull()
                .extracting("id", "slug", "canonicalPath", "title", "content")
                .containsExactly(
                        mockPost.getId(),
                        mockPost.getSlug(),
                        "/posts/" + mockPost.getSlug(),
                        mockPost.getTitle(),
                        mockPost.getContent()
                );
    }

    @Test
    public void throwPostNotFoundExceptionWhenRepositoryReturnsNullPostForGetPostBySlug() {
        // given
        when(postRepository.findBySlug("missing-slug")).thenReturn(null);

        // then
        assertThatThrownBy(() -> publicService.getPostBySlug("missing-slug"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void resolveSlugByIdShouldReturnSlug() {
        Post post = buildPost(builder -> builder.views(5L));
        when(postRepository.findById(post.getId())).thenReturn(post);

        String slug = publicService.resolveSlugById(post.getId());

        assertThat(slug).isEqualTo(post.getSlug());
    }

    @Test
    public void resolveSlugByIdShouldThrowWhenPostMissing() {
        when(postRepository.findById(555)).thenReturn(null);

        assertThatThrownBy(() -> publicService.resolveSlugById(555))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void getCanonicalPathsShouldReturnSlugPaths() {
        when(postRepository.findAllSlugs()).thenReturn(List.of("sample-post-title", "another-slug"));

        List<String> paths = publicService.getCanonicalPaths();

        assertThat(paths).containsExactly("/posts/sample-post-title", "/posts/another-slug");
    }

    @Test
    public void createAndReturnListOfCommentResponse() {
        // given
        Post post = buildPost();
        List<CommentResponse> expected = List.of(
                CommentResponse.builder().authorName("Alice").content("정말 좋은 포스트네요!").build(),
                CommentResponse.builder().authorName("Bob").content("유익한 정보 감사합니다.").build(),
                CommentResponse.builder().authorName("Charlie").content("더 많은 글 기대할게요.").build()
        );
        List<Comment> commentList = List.of(
                buildComment(post, builder -> builder.id(1L).authorName("Alice").content("정말 좋은 포스트네요!")),
                buildComment(post, builder -> builder.id(2L).authorName("Bob").content("유익한 정보 감사합니다.")),
                buildComment(post, builder -> builder.id(3L).authorName("Charlie").content("더 많은 글 기대할게요."))
        );

        commentList.forEach(comment -> {
            assertThat(comment).isNotNull();
            assertThat(comment.getCreatedAt()).isNotNull();
        });

        Integer postId = post.getId();
        assertThat(postId).isNotNull();

        when(postRepository.findById(postId)).thenReturn(post);
        when(commentRepository.findAllByPost(post)).thenReturn(commentList);

        // when
        List<CommentResponse> result = publicService.getCommentsById(postId);

        // then
        assertThat(result)
                .isNotNull()
                .hasSize(3)
                .extracting("authorName", "content")
                .containsExactly(
                        tuple(expected.get(0).getAuthorName(), expected.get(0).getContent()),
                        tuple(expected.get(1).getAuthorName(), expected.get(1).getContent()),
                        tuple(expected.get(2).getAuthorName(), expected.get(2).getContent())
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
        var commentRequest = buildCommentRequest(builder -> builder
                .author("peter")
                .content("content of mock comment request"));
        Post post = buildPost();

        when(postRepository.findById(post.getId())).thenReturn(post);
        when(commentRepository.save(any(Comment.class)))
                .thenReturn(buildComment(post, builder -> builder
                        .id(1L)
                        .authorName(commentRequest.getAuthor())
                        .content(commentRequest.getContent())
                ));

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
        var commentRequest = buildCommentRequest(builder -> builder
                .author("peter")
                .content("content of mock comment request"));
        when(postRepository.findById(postId)).thenReturn(null);

        // then
        assertThatThrownBy(() -> publicService.createComment(postId, commentRequest))
                .isInstanceOf(NoSuchElementException.class);
    }


    @Test
    public void incrementPostViewsShouldIncreaseViewsCount() {
        // given
        Post post = buildPost(builder -> builder.views(5L));
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
        List<Post> postList = List.of(buildPost(builder -> builder.views(5L)));
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
        List<Post> postList = List.of(buildPost());
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
    public void throwTagNotFoundExceptionWhenTagDoesNotExistForGetPostsByTag() {
        // given
        String tagName = "unknown-tag";
        when(tagRepository.findByName(tagName)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> publicService.getPostsByTag(tagName, 0, 10, "createdAt,asc"))
                .isInstanceOf(NoSuchElementException.class);

        verify(postRepository, never()).findAllByTagName(anyString(), anyInt(), anyInt(), anyString());
        verify(postRepository, never()).countByTagName(anyString());
    }


}
