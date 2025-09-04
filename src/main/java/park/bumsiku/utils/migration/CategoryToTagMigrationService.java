package park.bumsiku.utils.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import park.bumsiku.domain.entity.Category;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.domain.entity.Tag;
import park.bumsiku.repository.CategoryRepository;
import park.bumsiku.repository.PostRepository;
import park.bumsiku.repository.TagRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    value = "migration.categories-to-tags.enabled",
    havingValue = "true",
    matchIfMissing = false
)
@RequiredArgsConstructor
public class CategoryToTagMigrationService implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption("migrate-categories-to-tags")) {
            log.info("Starting category to tag migration...");
            migrateCategoriesAsTagsToAllPosts();
            log.info("Category to tag migration completed successfully");
        }
    }

    public void migrateCategoriesAsTagsToAllPosts() {
        List<Category> categories = categoryRepository.findAll();
        
        log.info("Found {} categories to migrate", categories.size());

        for (Category category : categories) {
            String tagName = category.getName();
            
            Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                    .orElseGet(() -> {
                        log.info("Creating new tag for category: {}", tagName);
                        Tag newTag = Tag.builder()
                                .name(tagName)
                                .build();
                        return tagRepository.save(newTag);
                    });

            // Find all posts with this category and add the corresponding tag
            List<Post> postsInCategory = findPostsByCategoryId(category.getId());
            
            log.info("Adding tag '{}' to {} posts", tagName, postsInCategory.size());
            
            for (Post post : postsInCategory) {
                if (!post.getTags().contains(tag)) {
                    post.addTag(tag);
                    postRepository.update(post);
                    log.debug("Added tag '{}' to post: {}", tagName, post.getTitle());
                }
            }
        }
        
        log.info("Successfully migrated all categories as tags to their respective posts");
    }

    public void rollbackCategoryToTagMigration() {
        log.info("Starting rollback of category to tag migration...");
        
        List<Category> categories = categoryRepository.findAll();
        
        for (Category category : categories) {
            String tagName = category.getName();
            Optional<Tag> tagOptional = tagRepository.findByNameIgnoreCase(tagName);
            
            if (tagOptional.isPresent()) {
                Tag tag = tagOptional.get();
                
                // Remove this tag from all posts that have the same category
                List<Post> postsInCategory = findPostsByCategoryId(category.getId());
                
                for (Post post : postsInCategory) {
                    if (post.getTags().contains(tag)) {
                        post.removeTag(tag);
                        postRepository.update(post);
                        log.debug("Removed tag '{}' from post: {}", tagName, post.getTitle());
                    }
                }
                
                // If the tag has no posts left, delete it
                if (tag.getPosts().isEmpty()) {
                    tagRepository.delete(tag);
                    log.info("Deleted empty tag: {}", tagName);
                }
            }
        }
        
        log.info("Successfully rolled back category to tag migration");
    }

    private List<Post> findPostsByCategoryId(Integer categoryId) {
        return postRepository.findAllByCategoryId(categoryId, 0, Integer.MAX_VALUE);
    }

    public MigrationStatus getMigrationStatus() {
        List<Category> categories = categoryRepository.findAll();
        int totalCategories = categories.size();
        int migratedCategories = 0;
        int totalPosts = 0;
        int migratedPosts = 0;

        for (Category category : categories) {
            Optional<Tag> correspondingTag = tagRepository.findByNameIgnoreCase(category.getName());
            
            if (correspondingTag.isPresent()) {
                migratedCategories++;
                
                List<Post> postsInCategory = findPostsByCategoryId(category.getId());
                totalPosts += postsInCategory.size();
                
                for (Post post : postsInCategory) {
                    if (post.getTags().contains(correspondingTag.get())) {
                        migratedPosts++;
                    }
                }
            } else {
                List<Post> postsInCategory = findPostsByCategoryId(category.getId());
                totalPosts += postsInCategory.size();
            }
        }

        return MigrationStatus.builder()
                .totalCategories(totalCategories)
                .migratedCategories(migratedCategories)
                .totalPosts(totalPosts)
                .migratedPosts(migratedPosts)
                .migrationCompleted(migratedCategories == totalCategories && migratedPosts == totalPosts)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class MigrationStatus {
        private int totalCategories;
        private int migratedCategories;
        private int totalPosts;
        private int migratedPosts;
        private boolean migrationCompleted;
        
        public double getCategoryMigrationPercentage() {
            return totalCategories == 0 ? 100.0 : (migratedCategories * 100.0) / totalCategories;
        }
        
        public double getPostMigrationPercentage() {
            return totalPosts == 0 ? 100.0 : (migratedPosts * 100.0) / totalPosts;
        }
    }
}