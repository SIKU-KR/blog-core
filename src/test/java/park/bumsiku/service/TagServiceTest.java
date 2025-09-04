package park.bumsiku.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import park.bumsiku.domain.dto.request.CreateTagRequest;
import park.bumsiku.domain.dto.request.UpdateTagRequest;
import park.bumsiku.domain.dto.response.TagResponse;
import park.bumsiku.domain.entity.Tag;
import park.bumsiku.repository.TagRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private Tag springTag;
    private Tag javaTag;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        springTag = Tag.builder()
                .id(1)
                .name("Spring")
                .createdAt(now)
                .build();
        
        javaTag = Tag.builder()
                .id(2)
                .name("Java")
                .createdAt(now.minusHours(1))
                .build();
    }

    @Test
    @DisplayName("getAllTags should return all tags ordered by name")
    void getAllTags_shouldReturnAllTagsOrderedByName() {
        // given
        List<Tag> tags = List.of(springTag, javaTag);
        when(tagRepository.findAllByOrderByNameAsc()).thenReturn(tags);

        // when
        List<TagResponse> result = tagService.getAllTags();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Spring");
        assertThat(result.get(1).getName()).isEqualTo("Java");
        verify(tagRepository).findAllByOrderByNameAsc();
    }

    @Test
    @DisplayName("getTagById should return tag when tag exists")
    void getTagById_whenTagExists_shouldReturnTag() {
        // given
        when(tagRepository.findById(1)).thenReturn(Optional.of(springTag));

        // when
        TagResponse result = tagService.getTagById(1);

        // then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Spring");
        verify(tagRepository).findById(1);
    }

    @Test
    @DisplayName("getTagById should throw exception when tag does not exist")
    void getTagById_whenTagDoesNotExist_shouldThrowException() {
        // given
        when(tagRepository.findById(999)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tagService.getTagById(999))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Tag not found with id: 999");

        verify(tagRepository).findById(999);
    }

    @Test
    @DisplayName("createTag should create new tag when name does not exist")
    void createTag_whenNameDoesNotExist_shouldCreateNewTag() {
        // given
        CreateTagRequest request = new CreateTagRequest("React");
        Tag newTag = Tag.builder()
                .id(3)
                .name("React")
                .createdAt(now)
                .build();

        when(tagRepository.existsByNameIgnoreCase("React")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(newTag);

        // when
        TagResponse result = tagService.createTag(request);

        // then
        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("React");

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(tagCaptor.capture());
        Tag capturedTag = tagCaptor.getValue();
        assertThat(capturedTag.getName()).isEqualTo("React");
    }

    @Test
    @DisplayName("createTag should throw exception when tag name already exists")
    void createTag_whenNameAlreadyExists_shouldThrowException() {
        // given
        CreateTagRequest request = new CreateTagRequest("Spring");
        when(tagRepository.existsByNameIgnoreCase("Spring")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> tagService.createTag(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag with name 'Spring' already exists");

        verify(tagRepository).existsByNameIgnoreCase("Spring");
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    @DisplayName("updateTag should update tag when tag exists and new name is unique")
    void updateTag_whenTagExistsAndNewNameIsUnique_shouldUpdateTag() {
        // given
        UpdateTagRequest request = new UpdateTagRequest("SpringBoot");
        Tag updatedTag = Tag.builder()
                .id(1)
                .name("SpringBoot")
                .createdAt(springTag.getCreatedAt())
                .build();

        when(tagRepository.findById(1)).thenReturn(Optional.of(springTag));
        when(tagRepository.existsByNameIgnoreCase("SpringBoot")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(updatedTag);

        // when
        TagResponse result = tagService.updateTag(1, request);

        // then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("SpringBoot");

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(tagCaptor.capture());
        Tag capturedTag = tagCaptor.getValue();
        assertThat(capturedTag.getName()).isEqualTo("SpringBoot");
    }

    @Test
    @DisplayName("updateTag should throw exception when tag does not exist")
    void updateTag_whenTagDoesNotExist_shouldThrowException() {
        // given
        UpdateTagRequest request = new UpdateTagRequest("NewName");
        when(tagRepository.findById(999)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tagService.updateTag(999, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Tag not found with id: 999");

        verify(tagRepository).findById(999);
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    @DisplayName("updateTag should throw exception when new name already exists")
    void updateTag_whenNewNameAlreadyExists_shouldThrowException() {
        // given
        UpdateTagRequest request = new UpdateTagRequest("Java");
        when(tagRepository.findById(1)).thenReturn(Optional.of(springTag));
        when(tagRepository.existsByNameIgnoreCase("Java")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> tagService.updateTag(1, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag with name 'Java' already exists");

        verify(tagRepository).findById(1);
        verify(tagRepository).existsByNameIgnoreCase("Java");
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    @DisplayName("deleteTag should delete tag when tag exists")
    void deleteTag_whenTagExists_shouldDeleteTag() {
        // given
        when(tagRepository.findById(1)).thenReturn(Optional.of(springTag));
        doNothing().when(tagRepository).deleteById(1);

        // when
        tagService.deleteTag(1);

        // then
        verify(tagRepository).findById(1);
        verify(tagRepository).deleteById(1);
    }

    @Test
    @DisplayName("deleteTag should throw exception when tag does not exist")
    void deleteTag_whenTagDoesNotExist_shouldThrowException() {
        // given
        when(tagRepository.findById(999)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tagService.deleteTag(999))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Tag not found with id: 999");

        verify(tagRepository).findById(999);
        verify(tagRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("findOrCreateTags should return existing tags and create new ones")
    void findOrCreateTags_shouldReturnExistingTagsAndCreateNewOnes() {
        // given
        List<String> tagNames = List.of("Spring", "React", "Vue");
        
        // Mock existing tags
        when(tagRepository.findByNameIn(tagNames)).thenReturn(List.of(springTag));
        
        // Mock tag existence checks for new tags only (Spring already exists)
        when(tagRepository.existsByNameIgnoreCase("React")).thenReturn(false);
        when(tagRepository.existsByNameIgnoreCase("Vue")).thenReturn(false);
        
        // Mock new tag creation
        Tag reactTag = Tag.builder().id(3).name("React").createdAt(now).build();
        Tag vueTag = Tag.builder().id(4).name("Vue").createdAt(now).build();
        when(tagRepository.save(any(Tag.class)))
                .thenReturn(reactTag)
                .thenReturn(vueTag);

        // when
        Set<Tag> result = tagService.findOrCreateTags(tagNames);

        // then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder("Spring", "React", "Vue");

        verify(tagRepository).findByNameIn(tagNames);
        verify(tagRepository, times(2)).save(any(Tag.class));
    }

    @Test
    @DisplayName("findOrCreateTags should handle empty list")
    void findOrCreateTags_whenEmptyList_shouldReturnEmptySet() {
        // given
        List<String> emptyTagNames = List.of();

        // when
        Set<Tag> result = tagService.findOrCreateTags(emptyTagNames);

        // then
        assertThat(result).isEmpty();
        verify(tagRepository, never()).findByNameIn(any());
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    @DisplayName("getTagsByNames should return tags that exist")
    void getTagsByNames_shouldReturnExistingTags() {
        // given
        List<String> tagNames = List.of("Spring", "Java", "NonExistent");
        when(tagRepository.findByNameIn(tagNames)).thenReturn(List.of(springTag, javaTag));

        // when
        List<TagResponse> result = tagService.getTagsByNames(tagNames);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(TagResponse::getName)
                .containsExactlyInAnyOrder("Spring", "Java");
        
        verify(tagRepository).findByNameIn(tagNames);
    }
}