package park.bumsiku.support;

import park.bumsiku.domain.dto.request.CommentRequest;
import park.bumsiku.domain.dto.request.CreatePostRequest;
import park.bumsiku.domain.dto.request.UpdatePostRequest;
import park.bumsiku.domain.dto.response.CommentResponse;
import park.bumsiku.domain.dto.response.PostResponse;
import park.bumsiku.domain.entity.Comment;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.domain.entity.Tag;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class TestFixtures {

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    private TestFixtures() {
    }

    public static Post.PostBuilder postBuilder() {
        int sequence = SEQ.getAndIncrement();
        LocalDateTime now = LocalDateTime.now();
        return Post.builder()
                .id(sequence)
                .slug("test-post-" + sequence)
                .title("Test Post " + sequence)
                .content("Content for test post " + sequence)
                .summary("Summary for test post " + sequence)
                .state("published")
                .views(0L)
                .createdAt(now)
                .updatedAt(now)
                .tags(new HashSet<>());
    }

    public static Post buildPost() {
        return postBuilder().build();
    }

    public static Post buildPost(Consumer<Post.PostBuilder> customizer) {
        Post.PostBuilder builder = postBuilder();
        if (customizer != null) {
            customizer.accept(builder);
        }
        return builder.build();
    }

    public static Comment.CommentBuilder commentBuilder(Post post) {
        int sequence = SEQ.getAndIncrement();
        return Comment.builder()
                .id(null)
                .post(post)
                .authorName("Commenter " + sequence)
                .content("Comment content " + sequence)
                .createdAt(LocalDateTime.now());
    }

    public static Comment buildComment(Post post) {
        return commentBuilder(post).build();
    }

    public static Comment buildComment(Post post, Consumer<Comment.CommentBuilder> customizer) {
        Comment.CommentBuilder builder = commentBuilder(post);
        if (customizer != null) {
            customizer.accept(builder);
        }
        return builder.build();
    }

    public static CreatePostRequest.CreatePostRequestBuilder createPostRequestBuilder() {
        int sequence = SEQ.getAndIncrement();
        return CreatePostRequest.builder()
                .title("New Post " + sequence)
                .content("Content for new post " + sequence)
                .summary("Summary for new post " + sequence)
                .slug("new-post-" + sequence)
                .tags(List.of("tag" + sequence));
    }

    public static CreatePostRequest buildCreatePostRequest() {
        return createPostRequestBuilder().build();
    }

    public static CreatePostRequest buildCreatePostRequest(Consumer<CreatePostRequest.CreatePostRequestBuilder> customizer) {
        CreatePostRequest.CreatePostRequestBuilder builder = createPostRequestBuilder();
        if (customizer != null) {
            customizer.accept(builder);
        }
        return builder.build();
    }

    public static UpdatePostRequest.UpdatePostRequestBuilder updatePostRequestBuilder() {
        int sequence = SEQ.getAndIncrement();
        return UpdatePostRequest.builder()
                .title("Updated Post " + sequence)
                .content("Updated content " + sequence)
                .summary("Updated summary " + sequence)
                .slug("updated-post-" + sequence)
                .tags(List.of("tag" + sequence));
    }

    public static UpdatePostRequest buildUpdatePostRequest() {
        return updatePostRequestBuilder().build();
    }

    public static UpdatePostRequest buildUpdatePostRequest(Consumer<UpdatePostRequest.UpdatePostRequestBuilder> customizer) {
        UpdatePostRequest.UpdatePostRequestBuilder builder = updatePostRequestBuilder();
        if (customizer != null) {
            customizer.accept(builder);
        }
        return builder.build();
    }

    public static CommentRequest.CommentRequestBuilder commentRequestBuilder() {
        int sequence = SEQ.getAndIncrement();
        return CommentRequest.builder()
                .author("Author " + sequence)
                .content("Comment body " + sequence);
    }

    public static CommentRequest buildCommentRequest() {
        return commentRequestBuilder().build();
    }

    public static CommentRequest buildCommentRequest(Consumer<CommentRequest.CommentRequestBuilder> customizer) {
        CommentRequest.CommentRequestBuilder builder = commentRequestBuilder();
        if (customizer != null) {
            customizer.accept(builder);
        }
        return builder.build();
    }

    public static PostResponse buildPostResponse(Consumer<Post.PostBuilder> customizer) {
        Post post = buildPost(customizer);
        return PostResponse.builder()
                .id(post.getId())
                .slug(post.getSlug())
                .title(post.getTitle())
                .content(post.getContent())
                .summary(post.getSummary())
                .tags(post.getTags().stream().map(Tag::getName).toList())
                .views(post.getViews())
                .createdAt(post.getCreatedAt().toString())
                .updatedAt(post.getUpdatedAt().toString())
                .canonicalPath("/posts/" + post.getSlug())
                .build();
    }

    public static CommentResponse buildCommentResponse(Consumer<CommentResponse.CommentResponseBuilder> customizer) {
        int sequence = SEQ.getAndIncrement();
        CommentResponse.CommentResponseBuilder builder = CommentResponse.builder()
                .id(sequence)
                .authorName("Author " + sequence)
                .content("Comment body " + sequence)
                .createdAt(LocalDateTime.now().toString());
        if (customizer != null) {
            customizer.accept(builder);
        }
        return builder.build();
    }
}
