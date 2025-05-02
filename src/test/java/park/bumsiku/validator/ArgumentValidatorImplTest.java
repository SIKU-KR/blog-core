package park.bumsiku.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import park.bumsiku.config.MethodValidationTestConfig;
import park.bumsiku.domain.dto.CommentRequest;
import park.bumsiku.domain.dto.CreatePostRequest;
import park.bumsiku.domain.dto.UpdateCategoryRequest;
import park.bumsiku.domain.dto.UpdatePostRequest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(MethodValidationTestConfig.class)
@ActiveProfiles("test")
public class ArgumentValidatorImplTest {

    private ArgumentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ArgumentValidatorImpl();
    }

    @Test
    void testValidatePostId() {
        // Valid post ID
        assertDoesNotThrow(() -> validator.validatePostId(1));

        // Invalid post ID
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostId(0));
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostId(-1));
    }

    @Test
    void testValidateCommentId() {
        // Valid comment ID
        assertDoesNotThrow(() -> validator.validateCommentId("1"));

        // Invalid comment ID
        assertThrows(IllegalArgumentException.class, () -> validator.validateCommentId("0"));
        assertThrows(IllegalArgumentException.class, () -> validator.validateCommentId("-1"));
        assertThrows(IllegalArgumentException.class, () -> validator.validateCommentId("abc"));
        assertThrows(IllegalArgumentException.class, () -> validator.validateCommentId(null));
    }

    @Test
    void testValidateCategoryId() {
        // Valid category ID
        assertDoesNotThrow(() -> validator.validateCategoryId(1));

        // Invalid category ID - null
        assertThrows(IllegalArgumentException.class, () -> validator.validateCategoryId(null));
    }

    @Test
    void testValidatePagination() {
        // Valid pagination
        assertDoesNotThrow(() -> validator.validatePagination(0, 10));
        assertDoesNotThrow(() -> validator.validatePagination(1, 20));

        // Invalid pagination
        assertThrows(IllegalArgumentException.class, () -> validator.validatePagination(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> validator.validatePagination(0, 0));
        assertThrows(IllegalArgumentException.class, () -> validator.validatePagination(0, -1));
    }

    @Test
    void testValidatePostRequest_Create() {
        // Valid post request
        CreatePostRequest validRequest = CreatePostRequest.builder()
                .title("Valid Title")
                .content("Valid content")
                .summary("Valid summary")
                .category("Technology")
                .build();
        assertDoesNotThrow(() -> validator.validatePostRequest(validRequest));

        // Invalid post request - null
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostRequest((CreatePostRequest) null));

        // Invalid post request - blank title
        CreatePostRequest invalidTitle = CreatePostRequest.builder()
                .title("")
                .content("Valid content")
                .summary("Valid summary")
                .category("Technology")
                .build();
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostRequest(invalidTitle));

        // Invalid post request - blank content
        CreatePostRequest invalidContent = CreatePostRequest.builder()
                .title("Valid Title")
                .content("")
                .summary("Valid summary")
                .category("Technology")
                .build();
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostRequest(invalidContent));

        // Invalid post request - blank summary
        CreatePostRequest invalidSummary = CreatePostRequest.builder()
                .title("Valid Title")
                .content("Valid content")
                .summary("")
                .category("Technology")
                .build();
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostRequest(invalidSummary));

        // Invalid post request - blank category
        CreatePostRequest invalidCategory = CreatePostRequest.builder()
                .title("Valid Title")
                .content("Valid content")
                .summary("Valid summary")
                .category("")
                .build();
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostRequest(invalidCategory));
    }

    @Test
    void testValidatePostRequest_Update() {
        // Valid post request
        UpdatePostRequest validRequest = UpdatePostRequest.builder()
                .title("Valid Title")
                .content("Valid content")
                .summary("Valid summary")
                .category("Technology")
                .build();
        assertDoesNotThrow(() -> validator.validatePostRequest(validRequest));

        // Invalid post request - null
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostRequest((UpdatePostRequest) null));

        // Other invalid cases would be similar to the create test
    }

    @Test
    void testValidateCategoryRequest() {
        // Valid category request
        UpdateCategoryRequest validRequest = UpdateCategoryRequest.builder()
                .id(1)
                .category("Technology")
                .order(1)
                .build();
        assertDoesNotThrow(() -> validator.validateCategoryRequest(validRequest));

        // Invalid category request - null
        assertThrows(IllegalArgumentException.class, () -> validator.validateCategoryRequest(null));

        // Invalid category request - null id
        UpdateCategoryRequest invalidId = UpdateCategoryRequest.builder()
                .id(null)
                .category("Technology")
                .order(1)
                .build();
        assertThrows(IllegalArgumentException.class, () -> validator.validateCategoryRequest(invalidId));

        // Invalid category request - blank category
        UpdateCategoryRequest invalidCategory = UpdateCategoryRequest.builder()
                .id(1)
                .category("")
                .order(1)
                .build();
        assertThrows(IllegalArgumentException.class, () -> validator.validateCategoryRequest(invalidCategory));

        // Invalid category request - null order
        UpdateCategoryRequest invalidOrder = UpdateCategoryRequest.builder()
                .id(1)
                .category("Technology")
                .order(null)
                .build();
        assertThrows(IllegalArgumentException.class, () -> validator.validateCategoryRequest(invalidOrder));
    }

    @Test
    void testValidatePostIdAndCommentRequest() {
        // Valid post ID and comment request
        CommentRequest validRequest = CommentRequest.builder()
                .content("Valid comment")
                .author("John")
                .build();
        assertDoesNotThrow(() -> validator.validatePostIdAndCommentRequest(1, validRequest));

        // Invalid post ID
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostIdAndCommentRequest(0, validRequest));

        // Invalid comment request - null
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostIdAndCommentRequest(1, null));

        // Invalid comment request - blank content
        CommentRequest invalidContent = CommentRequest.builder()
                .content("")
                .author("John")
                .build();
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostIdAndCommentRequest(1, invalidContent));

        // Invalid comment request - blank author
        CommentRequest invalidAuthor = CommentRequest.builder()
                .content("Valid comment")
                .author("")
                .build();
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostIdAndCommentRequest(1, invalidAuthor));
    }

    @Test
    void testValidatePostIdAndPostRequest() {
        // Valid post ID and post request
        UpdatePostRequest validRequest = UpdatePostRequest.builder()
                .title("Valid Title")
                .content("Valid content")
                .summary("Valid summary")
                .category("Technology")
                .build();
        assertDoesNotThrow(() -> validator.validatePostIdAndPostRequest(1, validRequest));

        // Invalid post ID
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostIdAndPostRequest(0, validRequest));

        // Invalid post request - null
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostIdAndPostRequest(1, null));

        // Other invalid cases would be similar to the validatePostRequest test
    }
}
