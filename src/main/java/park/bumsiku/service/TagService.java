package park.bumsiku.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import park.bumsiku.domain.dto.response.TagResponse;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.domain.entity.Tag;
import park.bumsiku.repository.TagRepository;
import park.bumsiku.utils.monitoring.LogExecutionTime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    // Helper methods to avoid duplication
    private boolean hasPosts(Tag tag) {
        return tag != null && tag.getPosts() != null && !tag.getPosts().isEmpty();
    }

    private List<String> safeTagNames(List<String> tagNames) {
        return tagNames == null ? List.of() : tagNames;
    }

    private Set<String> existingTagNameSet(List<Tag> existingTags) {
        return existingTags.stream().map(Tag::getName).collect(Collectors.toSet());
    }

    private List<String> findMissingTagNames(List<String> tagNames, Set<String> existingNames) {
        return tagNames.stream()
                .filter(name -> !existingNames.contains(name))
                .toList();
    }

    private void createAndAttachMissingTags(Set<Tag> collector, List<String> missingNames) {
        for (String tagName : missingNames) {
            String trimmedName = tagName == null ? "" : tagName.trim();
            if (!trimmedName.isEmpty() && !tagRepository.existsByNameIgnoreCase(trimmedName)) {
                Tag newTag = Tag.builder().name(trimmedName).build();
                Tag savedTag = tagRepository.save(newTag);
                collector.add(savedTag);
            }
        }
    }

    @LogExecutionTime
    public List<TagResponse> getAllActiveTagsWithPosts() {
        List<Tag> tags = tagRepository.findAllByOrderByNameAsc().stream()
                .filter(this::hasPosts)
                .toList();
        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    @LogExecutionTime
    @Transactional
    public Set<Tag> findOrCreateTags(List<String> tagNames) {
        List<String> inputNames = safeTagNames(tagNames);
        if (inputNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> collected = new HashSet<>();

        // Find existing tags
        List<Tag> existingTags = tagRepository.findByNameIn(inputNames);
        collected.addAll(existingTags);

        // Determine missing names and create them
        Set<String> existingNames = existingTagNameSet(existingTags);
        List<String> missingNames = findMissingTagNames(inputNames, existingNames);
        createAndAttachMissingTags(collected, missingNames);

        return collected;
    }

    @LogExecutionTime
    public List<TagResponse> getTagsByNames(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        List<Tag> tags = tagRepository.findByNameIn(tagNames);
        return tags.stream()
                .map(TagResponse::fromWithoutPostCount)
                .collect(Collectors.toList());
    }

    @LogExecutionTime
    @Transactional
    public void cleanupOrphanedTags() {
        List<Tag> allTags = tagRepository.findAll();
        List<Tag> orphanedTags = allTags.stream()
                .filter(tag -> !hasPosts(tag))
                .collect(Collectors.toList());

        if (!orphanedTags.isEmpty()) {
            log.info("Cleaning up {} orphaned tags", orphanedTags.size());
            tagRepository.deleteAll(orphanedTags);
        }
    }

    @LogExecutionTime
    @Transactional
    public void updatePostTags(Post post, List<String> newTagNames) {
        newTagNames = safeTagNames(newTagNames);

        // Clear existing tags
        post.clearTags();

        // Add new tags (create if they don't exist)
        Set<Tag> newTags = findOrCreateTags(newTagNames);
        newTags.forEach(post::addTag);

        // Clean up orphaned tags after update
        cleanupOrphanedTags();
    }
}
