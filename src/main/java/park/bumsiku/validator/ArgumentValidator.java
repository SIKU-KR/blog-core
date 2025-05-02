package park.bumsiku.validator;

import park.bumsiku.domain.dto.CommentRequest;
import park.bumsiku.domain.dto.CreatePostRequest;
import park.bumsiku.domain.dto.UpdateCategoryRequest;
import park.bumsiku.domain.dto.UpdatePostRequest;

public interface ArgumentValidator {

    void validatePostId(int postId);

    void validateCommentId(String commentId);

    void validateCategoryId(Integer id);

    void validatePagination(int page, int size);

    void validatePostRequest(CreatePostRequest request);

    void validatePostRequest(UpdatePostRequest request);

    void validateCategoryRequest(UpdateCategoryRequest request);

    void validatePostIdAndCommentRequest(int postId, CommentRequest request);

    void validatePostIdAndPostRequest(int postId, UpdatePostRequest request);
}
