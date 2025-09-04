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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    @LogExecutionTime
    public List<TagResponse> getAllActiveTagsWithPosts() {
        List<Tag> tags = tagRepository.findAllByOrderByNameAsc().stream()
                .filter(tag -> !tag.getPosts().isEmpty())
                .collect(Collectors.toList());
        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    @LogExecutionTime
    @Transactional
    public Set<Tag> findOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> tags = new HashSet<>();
        
        // Find existing tags
        List<Tag> existingTags = tagRepository.findByNameIn(tagNames);
        tags.addAll(existingTags);
        
        // Find missing tag names
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
        
        List<String> missingTagNames = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .collect(Collectors.toList());
        
        // Create new tags for missing names
        for (String tagName : missingTagNames) {
            String trimmedName = tagName.trim();
            if (!trimmedName.isEmpty() && !tagRepository.existsByNameIgnoreCase(trimmedName)) {
                Tag newTag = Tag.builder()
                        .name(trimmedName)
                        .build();
                Tag savedTag = tagRepository.save(newTag);
                tags.add(savedTag);
            }
        }
        
        return tags;
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
                .filter(tag -> tag.getPosts().isEmpty())
                .collect(Collectors.toList());
        
        if (!orphanedTags.isEmpty()) {
            log.info("Cleaning up {} orphaned tags", orphanedTags.size());
            tagRepository.deleteAll(orphanedTags);
        }
    }

    @LogExecutionTime
    @Transactional
    public void updatePostTags(Post post, List<String> newTagNames) {
        if (newTagNames == null) {
            newTagNames = List.of();
        }
        
        // Clear existing tags
        post.clearTags();
        
        // Add new tags (create if they don't exist)
        Set<Tag> newTags = findOrCreateTags(newTagNames);
        newTags.forEach(post::addTag);
        
        // Clean up orphaned tags after update
        cleanupOrphanedTags();
    }
}