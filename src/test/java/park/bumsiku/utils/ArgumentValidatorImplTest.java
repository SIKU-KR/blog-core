package park.bumsiku.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import park.bumsiku.config.AbstractTestSupport;
import park.bumsiku.config.MethodValidationTestConfig;
import park.bumsiku.domain.dto.request.*;
import park.bumsiku.utils.validation.ArgumentValidator;
import park.bumsiku.utils.validation.ArgumentValidatorImpl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import(MethodValidationTestConfig.class)
public class ArgumentValidatorImplTest extends AbstractTestSupport {

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
                .category(1)
                .build();
        assertDoesNotThrow(() -> validator.validatePostRequest(validRequest));

        // Invalid post request - null
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostRequest((CreatePostRequest) null));

        // Invalid post request - blank title
        CreatePostRequest invalidTitle = CreatePostRequest.builder()
                .title("")
                .content("Valid content")
                .summary("Valid summary")
                .category(1)
                .build();
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostRequest(invalidTitle));

        // Invalid post request - blank content
        CreatePostRequest invalidContent = CreatePostRequest.builder()
                .title("Valid Title")
                .content("")
                .summary("Valid summary")
                .category(1)
                .build();
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostRequest(invalidContent));

        // Invalid post request - blank summary
        CreatePostRequest invalidSummary = CreatePostRequest.builder()
                .title("Valid Title")
                .content("Valid content")
                .summary("")
                .category(1)
                .build();
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostRequest(invalidSummary));

        // Category is optional; absence should be allowed
        CreatePostRequest noCategory = CreatePostRequest.builder()
                .title("Valid Title")
                .content("Valid content")
                .summary("Valid summary")
                .build();
        assertDoesNotThrow(() -> validator.validatePostRequest(noCategory));
    }

    @Test
    void testValidatePostRequest_Update() {
        // Valid post request
        UpdatePostRequest validRequest = UpdatePostRequest.builder()
                .title("Valid Title")
                .content("Valid content")
                .summary("Valid summary")
                .category(3)
                .build();
        assertDoesNotThrow(() -> validator.validatePostRequest(validRequest));

        // Invalid post request - null
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostRequest((UpdatePostRequest) null));

        // Other invalid cases would be similar to the create test
    }

//    @Test
//    void testValidateCategoryRequest() {
//        // Valid category request
//        UpdateCategoryRequest validRequest = UpdateCategoryRequest.builder()
//                .name("Technology")
//                .orderNum(1)
//                .build();
//        assertDoesNotThrow(() -> validator.validateCategoryRequest(validRequest));
//
//        // Invalid category request - null
//        assertThrows(IllegalArgumentException.class, () -> validator.validateCategoryRequest((CreateCategoryRequest) null));
//        assertThrows(IllegalArgumentException.class, () -> validator.validateCategoryRequest((UpdateCategoryRequest) null));
//
//        // Invalid category request - blank category
//        UpdateCategoryRequest invalidCategory = UpdateCategoryRequest.builder()
//                .name("")
//                .orderNum(1)
//                .build();
//        assertThrows(IllegalArgumentException.class, () -> validator.validateCategoryRequest(invalidCategory));
//
//        // Invalid category request - null order
//        UpdateCategoryRequest invalidOrder = UpdateCategoryRequest.builder()
//                .name("Technology")
//                .orderNum(null)
//                .build();
//        assertThrows(IllegalArgumentException.class, () -> validator.validateCategoryRequest(invalidOrder));
//    }

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
                .category(3)
                .build();
        assertDoesNotThrow(() -> validator.validatePostIdAndPostRequest(1, validRequest));

        // Invalid post ID
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostIdAndPostRequest(0, validRequest));

        // Invalid post request - null
        assertThrows(IllegalArgumentException.class, () -> validator.validatePostIdAndPostRequest(1, null));

        // Other invalid cases would be similar to the validatePostRequest test
    }

    @Test
    void testValidateImage() {
        // Valid image
        MockMultipartFile validImage = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                new byte[1024] // 1KB
        );
        assertDoesNotThrow(() -> validator.validateImage(validImage));

        // Valid image with different extension
        MockMultipartFile validPngImage = new MockMultipartFile(
                "image",
                "test-image.png",
                "image/png",
                new byte[1024] // 1KB
        );
        assertDoesNotThrow(() -> validator.validateImage(validPngImage));

        // Null image
        assertThrows(IllegalArgumentException.class, () -> validator.validateImage(null));

        // Empty image
        MockMultipartFile emptyImage = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                new byte[0]
        );
        assertThrows(IllegalArgumentException.class, () -> validator.validateImage(emptyImage));

        // Invalid extension
        MockMultipartFile invalidExtensionImage = new MockMultipartFile(
                "image",
                "test-image.txt",
                "text/plain",
                new byte[1024]
        );
        assertThrows(IllegalArgumentException.class, () -> validator.validateImage(invalidExtensionImage));

        // Too large image
        MockMultipartFile tooLargeImage = Mockito.mock(MockMultipartFile.class);
        Mockito.when(tooLargeImage.isEmpty()).thenReturn(false);
        Mockito.when(tooLargeImage.getSize()).thenReturn(25 * 1024 * 1024L); // 6MB
        Mockito.when(tooLargeImage.getOriginalFilename()).thenReturn("large-image.jpg");
        assertThrows(IllegalArgumentException.class, () -> validator.validateImage(tooLargeImage));

        // Invalid filename (too long)
        StringBuilder longFilename = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            longFilename.append("a");
        }
        longFilename.append(".jpg");

        MockMultipartFile longFilenameImage = Mockito.mock(MockMultipartFile.class);
        Mockito.when(longFilenameImage.isEmpty()).thenReturn(false);
        Mockito.when(longFilenameImage.getSize()).thenReturn(1024L);
        Mockito.when(longFilenameImage.getOriginalFilename()).thenReturn(longFilename.toString());
        assertThrows(IllegalArgumentException.class, () -> validator.validateImage(longFilenameImage));

        // Null filename
        MockMultipartFile nullFilenameImage = Mockito.mock(MockMultipartFile.class);
        Mockito.when(nullFilenameImage.isEmpty()).thenReturn(false);
        Mockito.when(nullFilenameImage.getSize()).thenReturn(1024L);
        Mockito.when(nullFilenameImage.getOriginalFilename()).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> validator.validateImage(nullFilenameImage));
    }
}
