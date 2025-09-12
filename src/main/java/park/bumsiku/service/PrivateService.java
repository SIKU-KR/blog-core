package park.bumsiku.service;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import park.bumsiku.domain.dto.request.CreatePostRequest;
import park.bumsiku.domain.dto.request.UpdatePostRequest;
import park.bumsiku.domain.dto.response.PostResponse;
import park.bumsiku.domain.dto.response.UploadImageResponse;
import park.bumsiku.domain.entity.Comment;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.repository.CommentRepository;
import park.bumsiku.repository.ImageRepository;
import park.bumsiku.repository.PostRepository;
import park.bumsiku.utils.monitoring.LogExecutionTime;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class PrivateService {

    private CommentRepository commentRepository;
    private PostRepository postRepository;
    private ImageRepository imageRepository;
    private TagService tagService;


    @LogExecutionTime
    public void deleteComment(String commentId) {
        long id = Long.parseLong(commentId);

        Comment comment = commentRepository.findById(id).orElse(null);

        if (comment == null) {
            log.warn("Comment not found with id: {}", commentId);
            throw new NoSuchElementException("Comment not found with id: " + commentId);
        }

        commentRepository.deleteById(comment.getId());
    }

    @LogExecutionTime
    public UploadImageResponse uploadImage(MultipartFile image) {
        String filename = UUID.randomUUID() + ".webp";

        try (InputStream in = image.getInputStream()) {
            byte[] webpBytes = ImmutableImage.loader()
                    .fromStream(in)
                    .bytes(WebpWriter.DEFAULT);

            String url = imageRepository.insert(filename, webpBytes);

            return UploadImageResponse.builder()
                    .size(webpBytes.length)
                    .url(url)
                    .build();

        } catch (IOException e) {
            log.error("Image conversion and saving failed for file: {}", image.getOriginalFilename(), e);
            throw new RuntimeException("이미지 변환 및 저장 실패: " + e.getMessage(), e);
        }
    }

    @LogExecutionTime
    public PostResponse createPost(CreatePostRequest request) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .summary(request.getSummary())
                .state("published")
                .build();

        Post savedPost = postRepository.insert(post);

        // Handle tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            tagService.updatePostTags(savedPost, request.getTags());
            savedPost = postRepository.update(savedPost);
        }

        return PostResponse.builder()
                .id(savedPost.getId())
                .title(savedPost.getTitle())
                .content(savedPost.getContent())
                .summary(savedPost.getSummary())
                .tags(savedPost.getTags().stream().map(tag -> tag.getName()).toList())
                .views(savedPost.getViews())
                .createdAt(savedPost.getCreatedAt().toString())
                .updatedAt(savedPost.getUpdatedAt().toString())
                .build();
    }

    @LogExecutionTime
    public void deletePost(int postId) {
        Post post = postRepository.findById(postId);

        if (post == null) {
            log.warn("Post not found with id: {}", postId);
            throw new NoSuchElementException("Post not found with id: " + postId);
        }

        List<Comment> comments = commentRepository.findAllByPost(post);

        if (!comments.isEmpty()) {
            log.info("Deleting {} comments associated with post id: {}", comments.size(), postId);
            comments.forEach(comment -> commentRepository.deleteById(comment.getId()));
        }

        // Clear tags from post before deletion
        post.clearTags();
        postRepository.update(post);

        postRepository.delete(postId);

    }

    @LogExecutionTime
    public PostResponse updatePost(int postId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId);

        if (post == null) {
            log.warn("Post not found with id: {}", postId);
            throw new NoSuchElementException("Post not found with id: " + postId);
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary());
        post.setUpdatedAt(LocalDateTime.now());

        // Update tags - this will automatically clean up orphaned tags
        tagService.updatePostTags(post, request.getTags());

        Post updatedPost = postRepository.update(post);

        return PostResponse.builder()
                .id(updatedPost.getId())
                .title(updatedPost.getTitle())
                .content(updatedPost.getContent())
                .summary(updatedPost.getSummary())
                .tags(updatedPost.getTags().stream().map(tag -> tag.getName()).toList())
                .views(updatedPost.getViews())
                .createdAt(updatedPost.getCreatedAt().toString())
                .updatedAt(updatedPost.getUpdatedAt().toString())
                .build();
    }
}
