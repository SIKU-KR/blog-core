package park.bumsiku.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import park.bumsiku.domain.dto.request.CreateTagRequest;
import park.bumsiku.domain.dto.request.UpdateTagRequest;
import park.bumsiku.domain.dto.response.TagResponse;
import park.bumsiku.domain.entity.Tag;
import park.bumsiku.repository.TagRepository;
import park.bumsiku.utils.monitoring.LogExecutionTime;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    @LogExecutionTime
    public List<TagResponse> getAllTags() {
        List<Tag> tags = tagRepository.findAllByOrderByNameAsc();
        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    @LogExecutionTime
    public TagResponse getTagById(Integer id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tag not found with id: " + id));
        return TagResponse.from(tag);
    }

    @LogExecutionTime
    @Transactional
    public TagResponse createTag(CreateTagRequest request) {
        String tagName = request.getName().trim();
        
        if (tagRepository.existsByNameIgnoreCase(tagName)) {
            throw new IllegalArgumentException("Tag with name '" + tagName + "' already exists");
        }

        Tag tag = Tag.builder()
                .name(tagName)
                .build();

        Tag savedTag = tagRepository.save(tag);
        return TagResponse.fromWithoutPostCount(savedTag);
    }

    @LogExecutionTime
    @Transactional
    public TagResponse updateTag(Integer id, UpdateTagRequest request) {
        Tag existingTag = tagRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tag not found with id: " + id));

        String newName = request.getName().trim();
        
        if (tagRepository.existsByNameIgnoreCase(newName) && 
            !existingTag.getName().equalsIgnoreCase(newName)) {
            throw new IllegalArgumentException("Tag with name '" + newName + "' already exists");
        }

        existingTag.setName(newName);
        Tag updatedTag = tagRepository.save(existingTag);
        return TagResponse.from(updatedTag);
    }

    @LogExecutionTime
    @Transactional
    public void deleteTag(Integer id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tag not found with id: " + id));
        
        tagRepository.deleteById(id);
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
}