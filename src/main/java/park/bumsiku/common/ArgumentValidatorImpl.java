package park.bumsiku.common;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import park.bumsiku.domain.dto.request.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class ArgumentValidatorImpl implements ArgumentValidator {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp"));

    private static final long MAX_IMAGE_SIZE = 20 * 1024 * 1024; // 20MB

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목을 입력해주세요");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("제목은 1자 이상 100자 이하로 입력해주세요");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용을 입력해주세요");
        }
        if (content.length() > 10000) {
            throw new IllegalArgumentException("내용은 1자 이상 10000자 이하로 입력해주세요");
        }
    }

    private void validateSummary(String summary) {
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("요약을 입력해주세요");
        }
        if (summary.length() > 200) {
            throw new IllegalArgumentException("요약은 1자 이상 200자 이하로 입력해주세요");
        }
    }

    private void validateCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("카테고리를 선택해주세요");
        }
    }

    private void validateCommentContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("댓글은 1자 이상 500자 이하로 입력해주세요");
        }
    }

    private void validateCommentAuthor(String author) {
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("작성자 이름을 입력해주세요");
        }
        if (author.length() < 2 || author.length() > 20) {
            throw new IllegalArgumentException("작성자 이름은 2자 이상 20자 이하로 입력해주세요");
        }
    }

    @Override
    public void validateCategoryId(Integer id) {
        if (id == null || id == 0) {
            throw new IllegalArgumentException("카테고리를 선택해주세요");
        }
    }

    private void validateCategoryOrder(Integer order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
    }

    private void validatePage(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다");
        }
    }

    private void validatePageSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다");
        }
    }

    @Override
    public void validatePostId(int postId) {
        if (postId < 1) {
            throw new IllegalArgumentException("게시글 ID는 1 이상이어야 합니다");
        }
    }

    @Override
    public void validateCommentId(String commentId) {
        if (commentId == null) {
            throw new IllegalArgumentException("댓글 ID는 1 이상이어야 합니다");
        }

        try {
            int id = Integer.parseInt(commentId);
            if (id < 1) {
                throw new IllegalArgumentException("댓글 ID는 1 이상이어야 합니다");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("댓글 ID는 숫자여야 합니다");
        }
    }

    @Override
    public void validatePostRequest(CreatePostRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 정보가 없습니다");
        }
        validateTitle(request.getTitle());
        validateContent(request.getContent());
        validateSummary(request.getSummary());
        validateCategoryId(request.getCategory());
    }

    @Override
    public void validatePostRequest(UpdatePostRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 정보가 없습니다");
        }
        validateTitle(request.getTitle());
        validateContent(request.getContent());
        validateSummary(request.getSummary());
        validateCategory(request.getCategory());
    }

    private void validateCommentRequest(CommentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 정보가 없습니다");
        }
        validateCommentContent(request.getContent());
        validateCommentAuthor(request.getAuthor());
    }

    @Override
    public void validateCategoryRequest(CreateCategoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 정보가 없습니다");
        }
        validateCategory(request.getName());
        validateCategoryOrder(request.getOrderNum());
    }

    @Override
    public void validateCategoryRequest(UpdateCategoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 정보가 없습니다");
        }
        validateCategory(request.getName());
        validateCategoryOrder(request.getOrderNum());
    }

    @Override
    public void validatePagination(int page, int size) {
        validatePage(page);
        validatePageSize(size);
    }

    @Override
    public void validatePostIdAndCommentRequest(int postId, CommentRequest request) {
        validatePostId(postId);
        validateCommentRequest(request);
    }

    @Override
    public void validatePostIdAndPostRequest(int postId, UpdatePostRequest request) {
        validatePostId(postId);
        validatePostRequest(request);
    }

    @Override
    public void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일을 업로드해주세요");
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("이미지 크기는 20MB 이하여야 합니다");
        }
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다");
        }
        if (originalFilename.length() > 100) {
            throw new IllegalArgumentException("파일 이름은 100자 이하여야 합니다");
        }
        String extension = getFileExtension(originalFilename);
        if (extension == null || !ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. 지원 형식: jpg, jpeg, png, gif, webp");
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDotIndex + 1);
    }
}
