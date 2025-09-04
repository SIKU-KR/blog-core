package park.bumsiku.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import park.bumsiku.domain.dto.request.CreatePostRequest;
import park.bumsiku.domain.dto.request.UpdateCategoryRequest;
import park.bumsiku.domain.dto.request.UpdatePostRequest;
import park.bumsiku.domain.dto.response.PostResponse;
import park.bumsiku.domain.dto.response.UploadImageResponse;
import park.bumsiku.domain.entity.Category;
import park.bumsiku.domain.entity.Comment;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.repository.CategoryRepository;
import park.bumsiku.repository.CommentRepository;
import park.bumsiku.repository.ImageRepository;
import park.bumsiku.repository.PostRepository;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrivateServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private TagService tagService;

    @InjectMocks
    private PrivateService privateService;

    // Test data
    private Category techCategory;
    private Category lifeCategory;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        // Create test categories
        now = LocalDateTime.now();
        techCategory = new Category(1, "Tech", 1, now);
        lifeCategory = new Category(2, "Life", 2, now);
        List<Category> categories = new ArrayList<>();
        categories.add(techCategory);
        categories.add(lifeCategory);
    }

    // Helper methods for creating test data
    private Category createCategory(Integer id, String name, Integer orderNum) {
        return new Category(id, name, orderNum, LocalDateTime.now());
    }

    private Post createPost(Integer id, String title, String content, String summary, Category category) {
        LocalDateTime now = LocalDateTime.now();
        return Post.builder()
                .id(id)
                .title(title)
                .content(content)
                .summary(summary)
                .category(category)
                .createdAt(now)
                .updatedAt(now)
                .state("published")
                .build();
    }

    private Post createPost(Integer id, String title, String content, String summary, Category category,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        return Post.builder()
                .id(id)
                .title(title)
                .content(content)
                .summary(summary)
                .category(category)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .state("published")
                .build();
    }

    private Comment createComment(Long id, Post post, String authorName, String content) {
        return new Comment(id, post, authorName, content, LocalDateTime.now());
    }

    private void assertPostResponseMatchesPost(PostResponse response, Post post) {
        assertThat(response)
                .isNotNull()
                .extracting("id", "title", "content")
                .containsExactly(
                        post.getId(),
                        post.getTitle(),
                        post.getContent()
                );

        assertEquals(post.getCreatedAt().toString(), response.getCreatedAt());
        assertEquals(post.getUpdatedAt().toString(), response.getUpdatedAt());
    }

    private void verifyPostFields(Post post, Integer id, String title, String content, String summary, Category category) {
        assertEquals(id, post.getId());
        assertEquals(title, post.getTitle());
        assertEquals(content, post.getContent());
        assertEquals(summary, post.getSummary());
        assertEquals(category, post.getCategory());
    }

    @Test
    @DisplayName("updateCategory should insert or update category and return response")
    void updateCategory_shouldInsertOrUpdateAndReturnResponse() {
        // given
        String updatedName = "Updated Tech";
        UpdateCategoryRequest request = new UpdateCategoryRequest(updatedName, 3);

        Category updatedCategory = new Category(techCategory.getId(), updatedName, 3, now);

        // Mock repository behavior
        when(categoryRepository.findById(techCategory.getId())).thenReturn(techCategory); // Mock findById to return the category
        when(categoryRepository.update(any(Category.class))).thenReturn(updatedCategory); // Simulate successful update
        // when
        var result = privateService.updateCategory(techCategory.getId(), request);

        // then
        assertThat(result)
                .isNotNull()
                .extracting("id", "name", "order")
                .containsExactly(
                        techCategory.getId(),
                        updatedName,
                        3
                );
    }

    @Test
    @DisplayName("deleteComment should delete comment when comment exists")
    void deleteComment_whenCommentExists_shouldDeleteSuccessfully() {
        // given
        // Create a post
        Post post = Post.builder()
                .id(1)
                .title("Test Title")
                .content("Test Content")
                .summary("Test Summary")
                .category(techCategory)
                .state("published")
                .build();

        // Create a comment
        long commentId = 1L;
        Comment comment = new Comment(commentId, post, "tester", "hello world", now);

        // Mock repository behavior
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));
        doNothing().when(commentRepository).deleteById(commentId);

        // when
        privateService.deleteComment(String.valueOf(commentId));

        // then
        // Verify the mock was called
        verify(commentRepository).findById(commentId);
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    @DisplayName("deleteComment should throw NoSuchElementException when comment does not exist")
    void deleteComment_whenCommentNotExists_shouldThrowException() {
        // given
        long nonExistentCommentId = 999L;

        // Mock repository behavior
        when(commentRepository.findById(nonExistentCommentId)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> privateService.deleteComment(String.valueOf(nonExistentCommentId)))
                .isInstanceOf(NoSuchElementException.class);

        // Verify the mock was called
        verify(commentRepository).findById(nonExistentCommentId);
    }

//    @Test
//    void uploadImage_validImage_shouldSaveImageAndReturnResponse() throws Exception {
//        String filename = "test-image.jpg";
//        String contentType = MediaType.IMAGE_JPEG_VALUE;
//        long size = 1024L;
//        byte[] content = "test image content".getBytes();
//        String expectedUrl = "/images/" + filename;
//
//        given(mockMultipartFile.getOriginalFilename()).willReturn(filename);
//        given(mockMultipartFile.getContentType()).willReturn(contentType);
//        given(mockMultipartFile.getSize()).willReturn(size);
//        given(mockMultipartFile.getBytes()).willReturn(content);
//
//        Post mockPost = mock(Post.class);
//        PostImage savedImageEntity = new PostImage(
//                1,
//                mockPost,
//                expectedUrl,
//                filename,
//                contentType
//        );
//        given(imageRepository.insert(any(PostImage.class))).willReturn(savedImageEntity);
//
//        var result = privateService.uploadImage(mockMultipartFile);
//
//        Assertions.assertNotNull(result);
//        Assertions.assertEquals(filename, result.getFileName());
//        Assertions.assertEquals(contentType, result.getMimeType());
//        Assertions.assertEquals(size, result.getSize());
//        Assertions.assertTrue(result.getTimestamp() > 0);
//
//        verify(imageRepository).insert(postImageCaptor.capture());
//        PostImage capturedImage = postImageCaptor.getValue();
//        Assertions.assertEquals(filename, capturedImage.getFilename());
//    }


    @Test
    @DisplayName("uploadImage: 유효한 MultipartFile 을 WebP 로 변환 후 저장하고 URL 반환")
    void uploadImage_validImage_shouldConvertAndReturnUrl() throws Exception {
        // given: 테스트 리소스 로드
        try (InputStream in = getClass().getResourceAsStream("/images.jpeg")) {
            assertThat(in).as("테스트용 이미지(images.jpeg)가 resources 루트에 있어야 합니다").isNotNull();
            byte[] originalBytes = in.readAllBytes();
            MultipartFile multipartFile = new MockMultipartFile(
                    "image",
                    "images.jpeg",
                    "image/jpeg",
                    originalBytes
            );
            when(imageRepository.insert(anyString(), any(byte[].class)))
                    .thenReturn("test-url");

            // when
            UploadImageResponse response = privateService.uploadImage(multipartFile);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUrl()).isEqualTo("test-url");
            assertThat(response.getSize()).isGreaterThan(0);

            verify(imageRepository, times(1)).insert(
                    argThat(name -> name.toLowerCase().endsWith(".webp")),
                    any(byte[].class)
            );
        }
    }

    @Test
    @DisplayName("createPost should save post and return response when request is valid")
    void createPost_validRequest_shouldSavePostAndReturnResponse() {
        // given
        CreatePostRequest request = CreatePostRequest.builder()
                .title("New Post")
                .content("Content")
                .summary("Summary")
                .category(1)
                .build();

        Post expectedPost = Post.builder()
                .id(1)
                .title(request.getTitle())
                .content(request.getContent())
                .summary(request.getSummary())
                .category(techCategory)
                .createdAt(now)
                .updatedAt(now)
                .state("published")
                .build();

        // Mock repository behavior
        when(postRepository.insert(any(Post.class))).thenReturn(expectedPost);
        when(categoryRepository.findById(techCategory.getId())).thenReturn(techCategory);

        // when
        PostResponse result = privateService.createPost(request);

        // then
        // Verify response is not null and has expected values
        assertThat(result)
                .isNotNull()
                .extracting("title", "content")
                .containsExactly(
                        request.getTitle(),
                        request.getContent()
                );

        // Verify the mock was called
        verify(postRepository).insert(any(Post.class));
    }

    @Test
    @DisplayName("deletePost should delete post and related data when post exists")
    void deletePost_whenPostExists_shouldDeletePostAndRelatedData() {
        // given
        // Create a post
        int postId = 1;
        Post post = Post.builder()
                .id(postId)
                .title("Test Title")
                .content("Test Content")
                .summary("Test Summary")
                .category(techCategory)
                .state("published")
                .build();

        // Create a comment for the post
        long commentId = 1L;
        Comment comment = new Comment(commentId, post, "tester", "test comment", now);
        List<Comment> comments = List.of(comment);

        // Mock repository behavior
        when(postRepository.findById(postId)).thenReturn(post);
        when(commentRepository.findAllByPost(post)).thenReturn(comments);
        doNothing().when(commentRepository).deleteById(commentId);
        when(postRepository.update(any(Post.class))).thenReturn(post);
        doNothing().when(postRepository).delete(postId);

        // when
        privateService.deletePost(postId);

        // then
        // Verify the mocks were called
        verify(postRepository).findById(postId);
        verify(commentRepository).findAllByPost(post);
        verify(commentRepository).deleteById(commentId);
        verify(postRepository).delete(postId);
    }

    @Test
    @DisplayName("deletePost should throw PostNotFoundException when post does not exist")
    void deletePost_whenPostNotExists_shouldThrowPostNotFoundException() {
        // given
        int nonExistentPostId = 999;

        // Mock repository behavior
        when(postRepository.findById(nonExistentPostId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> privateService.deletePost(nonExistentPostId))
                .isInstanceOf(NoSuchElementException.class);

        // Verify the mock was called
        verify(postRepository).findById(nonExistentPostId);
    }

    @Test
    @DisplayName("updatePost should update post and return response when post exists")
    void updatePost_whenPostExists_shouldUpdatePostAndReturnResponse() {
        // given
        // Create a post
        int postId = 1;
        LocalDateTime originalUpdatedAt = now.minusHours(1);
        Post post = Post.builder()
                .id(postId)
                .title("Original Title")
                .content("Original Content")
                .summary("Original Summary")
                .category(techCategory)
                .createdAt(now.minusHours(2))
                .updatedAt(originalUpdatedAt)
                .state("published")
                .build();

        // Create update request
        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("Updated Title")
                .content("Updated Content")
                .summary("Updated Summary")
                .category(lifeCategory.getId())
                .build();

        // Create updated post
        Post updatedPost = Post.builder()
                .id(postId)
                .title(request.getTitle())
                .content(request.getContent())
                .summary(request.getSummary())
                .category(lifeCategory)
                .createdAt(post.getCreatedAt())
                .updatedAt(now)
                .state("published")
                .build();

        // Mock repository behavior
        when(postRepository.findById(postId)).thenReturn(post);
        when(categoryRepository.findById(request.getCategory())).thenReturn(lifeCategory);
        doNothing().when(tagService).updatePostTags(any(Post.class), any());
        when(postRepository.update(any(Post.class))).thenReturn(updatedPost);

        // when
        PostResponse result = privateService.updatePost(postId, request);

        // then
        // Verify response
        assertThat(result)
                .isNotNull()
                .extracting("id", "title", "content")
                .containsExactly(
                        postId,
                        request.getTitle(),
                        request.getContent()
                );

        // Verify the mocks were called
        verify(postRepository).findById(postId);
        verify(postRepository).update(any(Post.class));
    }

    @Test
    @DisplayName("updatePost should throw PostNotFoundException when post does not exist")
    void updatePost_whenPostNotExists_shouldThrowPostNotFoundException() {
        // given
        int nonExistentPostId = 999;
        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("Updated Title")
                .content("Updated Content")
                .summary("Updated Summary")
                .category(1)
                .build();

        // Mock repository behavior
        when(postRepository.findById(nonExistentPostId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> privateService.updatePost(nonExistentPostId, request))
                .isInstanceOf(NoSuchElementException.class);

        // Verify the mock was called
        verify(postRepository).findById(nonExistentPostId);
    }
}
