package park.bumsiku.utils.validation;

import org.springframework.web.multipart.MultipartFile;
import park.bumsiku.domain.dto.request.*;

public interface ArgumentValidator {

    void validatePostId(int postId);

    void validateCommentId(String commentId);

    void validateCategoryId(Integer id);

    void validatePagination(int page, int size);

    void validatePostRequest(CreatePostRequest request);

    void validatePostRequest(UpdatePostRequest request);

    void validateCategoryRequest(CreateCategoryRequest request);

    void validateCategoryRequest(UpdateCategoryRequest request);

    void validatePostIdAndCommentRequest(int postId, CommentRequest request);

    void validatePostIdAndPostRequest(int postId, UpdatePostRequest request);

    void validateImage(MultipartFile image);
}
