package park.bumsiku.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import park.bumsiku.domain.dto.*;
import park.bumsiku.repository.CategoryRepository;
import park.bumsiku.repository.CommentRepository;
import park.bumsiku.repository.PostRepository;

@Service
@Transactional
public class PrivateService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

//    @Autowired
//    private PostImageRepository imageRepository;

    public CategoryResponse updateCategory(UpdateCategoryRequest request) {
        // TODO: business logic 구현
        throw new UnsupportedOperationException("Not implemented");
    }

    public void deleteComment(String commentId) {
        // TODO: business logic 구현
        throw new UnsupportedOperationException("Not implemented");
    }

    public UploadImageResponse uploadImage(MultipartFile image) {
        // TODO: business logic 구현
        throw new UnsupportedOperationException("Not implemented");
    }

    public PostResponse createPost(CreatePostRequest request) {
        // TODO: business logic 구현
        throw new UnsupportedOperationException("Not implemented");
    }

    public void deletePost(int postId) {
        // TODO: business logic 구현
        throw new UnsupportedOperationException("Not implemented");
    }

    public PostResponse updatePost(int postId, UpdatePostRequest request) {
        // TODO: business logic 구현
        throw new UnsupportedOperationException("Not implemented");
    }
}