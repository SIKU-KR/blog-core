package park.bumsiku.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import park.bumsiku.domain.dto.CreatePostRequest;
import park.bumsiku.domain.dto.PostResponse;
import park.bumsiku.domain.dto.UpdateCategoryRequest;
import park.bumsiku.domain.dto.UpdatePostRequest;
import park.bumsiku.domain.entity.Category;
import park.bumsiku.domain.entity.Comment;
import park.bumsiku.domain.entity.Post;
//import park.bumsiku.domain.entity.PostImage;
import park.bumsiku.exception.PostNotFoundException;
import park.bumsiku.repository.CategoryRepository;
import park.bumsiku.repository.CommentRepository;
//import park.bumsiku.repository.PostImageRepository;
import park.bumsiku.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PrivateServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

//    @Mock
//    private PostImageRepository imageRepository;

    @Mock
    private MultipartFile mockMultipartFile;

    @InjectMocks
    private PrivateService privateService;

    @Captor
    private ArgumentCaptor<Category> categoryCaptor;

    @Captor
    private ArgumentCaptor<Post> postCaptor;

//    @Captor
//    private ArgumentCaptor<PostImage> postImageCaptor;

    @Test
    void updateCategory_shouldInsertOrUpdateAndReturnResponse() {
        UpdateCategoryRequest request = new UpdateCategoryRequest(1, "Updated Tech", 1);
        Category savedCategory = new Category(1, request.getCategory(), request.getOrder(), LocalDateTime.now());

        given(categoryRepository.insert(any(Category.class))).willReturn(savedCategory);
        given(categoryRepository.update(any(Category.class))).willReturn(0);

        var result = privateService.updateCategory(request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedCategory.getId().intValue(), result.getId());
        Assertions.assertEquals(request.getCategory(), result.getName());
        Assertions.assertEquals(request.getOrder(), result.getOrderNum());
        Assertions.assertEquals(savedCategory.getCreatedAt(), result.getCreatedAt());
    }

    @Test
    void deleteComment_whenCommentExists_shouldDeleteSuccessfully() {
        // given
        long commentId = 1L;
        Post mockPost = mock(Post.class);
        Comment existingComment =
                new Comment(
                        commentId,      // Long id
                        mockPost,       // Post post
                        "tester",       // String authorName
                        "hello world",  // String content
                        LocalDateTime.now()
                );

        given(commentRepository.findById(commentId)).willReturn(existingComment);
        doNothing().when(commentRepository).delete(existingComment.getId());

        // when
        privateService.deleteComment(String.valueOf(commentId));

        // then
        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(existingComment.getId());
    }
    @Test
    void deleteComment_whenCommentNotExists_shouldThrowException() {
        long commentId = 999L;
        given(commentRepository.findById(commentId)).willReturn(null);

        Assertions.assertThrows(
                NoSuchElementException.class,
                () -> privateService.deleteComment(String.valueOf(commentId))
        );

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(anyLong());
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
    void createPost_validRequest_shouldSavePostAndReturnResponse() {
        CreatePostRequest request = new CreatePostRequest("New Post", "Content", "Summary", "Tech");
        Category categoryEntity = new Category(1, request.getCategory(), 1, LocalDateTime.now());

        Post savedPost = Post.builder()
                .id(1)
                .title(request.getTitle())
                .content(request.getContent())
                .summary(request.getSummary())
                .category(categoryEntity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .state("published")
                .build();

        PostResponse expectedResponse = new PostResponse(
                savedPost.getId(),
                savedPost.getTitle(),
                savedPost.getContent(),
                savedPost.getCreatedAt().toString(),
                savedPost.getUpdatedAt().toString()
        );

        given(categoryRepository.findById(anyInt())).willReturn(categoryEntity);
        given(postRepository.insert(any(Post.class))).willReturn(savedPost);

        var result = privateService.createPost(request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedResponse.getId(), result.getId());
        Assertions.assertEquals(expectedResponse.getTitle(), result.getTitle());
        Assertions.assertEquals(expectedResponse.getContent(), result.getContent());
        Assertions.assertEquals(expectedResponse.getCreatedAt(), result.getCreatedAt());
        Assertions.assertEquals(expectedResponse.getUpdatedAt(), result.getUpdatedAt());

        verify(postRepository).insert(postCaptor.capture());
        Post capturedPost = postCaptor.getValue();
        Assertions.assertEquals(request.getTitle(), capturedPost.getTitle());
        Assertions.assertEquals(request.getContent(), capturedPost.getContent());
        Assertions.assertEquals(request.getSummary(), capturedPost.getSummary());
    }

    @Test
    void deletePost_whenPostExists_shouldDeletePostAndRelatedData() {
        int postId = 1;
        Post existingPost = Post.builder()
                .id(postId)
                .title("Test")
                .content("Test")
                .summary("Test")
                .category(mock(Category.class))
                .state("published")
                .build();

        given(postRepository.findById(postId)).willReturn(existingPost);
        given(commentRepository.findAllByPost(existingPost)).willReturn(Collections.emptyList());
        doNothing().when(postRepository).delete(postId);

        privateService.deletePost(postId);

        verify(postRepository).findById(postId);
        verify(commentRepository).findAllByPost(existingPost);
        verify(postRepository).delete(postId);
    }

    @Test
    void deletePost_whenPostNotExists_shouldThrowPostNotFoundException() {
        int postId = 999;
        given(postRepository.findById(postId)).willReturn(null);

        Assertions.assertThrows(
                PostNotFoundException.class,
                () -> privateService.deletePost(postId)
        );

        verify(postRepository).findById(postId);
        verify(postRepository, never()).delete(anyInt());
        verify(commentRepository, never()).findAllByPost(any());
    }

    @Test
    void updatePost_whenPostExists_shouldUpdatePostAndReturnResponse() {
        int postId = 1;
        UpdatePostRequest request = new UpdatePostRequest("Updated Title", "Updated Content", "Updated Summary", "Life");
        Category originalCategory = new Category(1, "Tech", 1, LocalDateTime.now().minusDays(1));
        Category newCategory = new Category(2, request.getCategory(), 2, LocalDateTime.now());
        Post existingPost = Post.builder()
                .id(postId)
                .title("Original Title")
                .content("Original Content")
                .summary("Original Summary")
                .category(originalCategory)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .state("published")
                .build();
        Post postAfterUpdate = Post.builder()
                .id(postId)
                .title(request.getTitle())
                .content(request.getContent())
                .summary(request.getSummary())
                .category(newCategory)
                .createdAt(existingPost.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .state("published")
                .build();

        PostResponse expectedResponse = new PostResponse(
                postAfterUpdate.getId(),
                postAfterUpdate.getTitle(),
                postAfterUpdate.getContent(),
                postAfterUpdate.getCreatedAt().toString(),
                postAfterUpdate.getUpdatedAt().toString()
        );

        given(postRepository.findById(postId)).willReturn(existingPost);
        given(categoryRepository.findById(anyInt())).willReturn(newCategory);
        given(postRepository.update(any(Post.class))).willReturn(postAfterUpdate);

        var result = privateService.updatePost(postId, request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedResponse.getId(), result.getId());
        Assertions.assertEquals(expectedResponse.getTitle(), result.getTitle());
        Assertions.assertEquals(expectedResponse.getContent(), result.getContent());
        Assertions.assertEquals(expectedResponse.getUpdatedAt(), result.getUpdatedAt());

        verify(postRepository).findById(postId);
        verify(categoryRepository).findById(anyInt());
        verify(postRepository).update(postCaptor.capture());
        Post capturedPost = postCaptor.getValue();
        Assertions.assertEquals(postId, capturedPost.getId());
        Assertions.assertEquals(request.getTitle(), capturedPost.getTitle());
        Assertions.assertEquals(request.getContent(), capturedPost.getContent());
        Assertions.assertEquals(request.getSummary(), capturedPost.getSummary());
        Assertions.assertTrue(capturedPost.getUpdatedAt().isAfter(existingPost.getUpdatedAt()));
    }

    @Test
    void updatePost_whenPostNotExists_shouldThrowPostNotFoundException() {
        int postId = 999;
        UpdatePostRequest request = new UpdatePostRequest("Updated Title", "Updated Content", "Updated Summary", "Life");
        given(postRepository.findById(postId)).willReturn(null);

        Assertions.assertThrows(
                PostNotFoundException.class,
                () -> privateService.updatePost(postId, request)
        );

        verify(postRepository).findById(postId);
        verify(postRepository, never()).update(any());
        verify(categoryRepository, never()).findById(anyInt());
    }
}