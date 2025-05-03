package park.bumsiku.validator;

import org.springframework.stereotype.Component;
import park.bumsiku.domain.dto.request.*;

@Component
public class ArgumentValidatorImpl implements ArgumentValidator {

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
}
