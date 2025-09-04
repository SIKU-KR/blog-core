package park.bumsiku.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import park.bumsiku.domain.dto.response.TagResponse;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.domain.entity.Tag;
import park.bumsiku.repository.TagRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private Tag springTag;
    private Tag javaTag;
    private Tag orphanedTag;
    private Post mockPost;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        mockPost = mock(Post.class);

        springTag = Tag.builder()
                .id(1)
                .name("Spring")
                .createdAt(now)
                .build();
        springTag.setPosts(Set.of(mockPost));

        javaTag = Tag.builder()
                .id(2)
                .name("Java")
                .createdAt(now.minusHours(1))
                .build();
        javaTag.setPosts(Set.of(mockPost));

        orphanedTag = Tag.builder()
                .id(3)
                .name("Orphaned")
                .createdAt(now.minusHours(2))
                .build();
        orphanedTag.setPosts(new HashSet<>());
    }

    @Test
    @DisplayName("getAllActiveTagsWithPosts should return only tags that have posts")
    void getAllActiveTagsWithPosts_shouldReturnOnlyTagsWithPosts() {
        // given
        List<Tag> allTags = List.of(springTag, javaTag, orphanedTag);
        when(tagRepository.findAllByOrderByNameAsc()).thenReturn(allTags);

        // when
        List<TagResponse> result = tagService.getAllActiveTagsWithPosts();

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(TagResponse::getName)
                .containsExactlyInAnyOrder("Spring", "Java");
        verify(tagRepository).findAllByOrderByNameAsc();
    }

    @Test
    @DisplayName("cleanupOrphanedTags should delete tags with no posts")
    void cleanupOrphanedTags_shouldDeleteTagsWithNoPosts() {
        // given
        List<Tag> allTags = List.of(springTag, javaTag, orphanedTag);
        when(tagRepository.findAll()).thenReturn(allTags);
        doNothing().when(tagRepository).deleteAll(any());

        // when
        tagService.cleanupOrphanedTags();

        // then
        ArgumentCaptor<List<Tag>> deletedTagsCaptor = ArgumentCaptor.forClass(List.class);
        verify(tagRepository).deleteAll(deletedTagsCaptor.capture());
        List<Tag> deletedTags = deletedTagsCaptor.getValue();

        assertThat(deletedTags).hasSize(1);
        assertThat(deletedTags.get(0).getName()).isEqualTo("Orphaned");
    }

    @Test
    @DisplayName("cleanupOrphanedTags should do nothing when all tags have posts")
    void cleanupOrphanedTags_whenAllTagsHavePosts_shouldDoNothing() {
        // given
        List<Tag> allTags = List.of(springTag, javaTag);
        when(tagRepository.findAll()).thenReturn(allTags);

        // when
        tagService.cleanupOrphanedTags();

        // then
        verify(tagRepository, never()).deleteAll(any());
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

    @Test
    @DisplayName("updatePostTags should clear existing tags and add new ones")
    void updatePostTags_shouldClearExistingTagsAndAddNewOnes() {
        // given
        Post post = mock(Post.class);
        List<String> newTagNames = List.of("Spring", "React");

        when(tagRepository.findByNameIn(newTagNames)).thenReturn(List.of(springTag));
        when(tagRepository.existsByNameIgnoreCase("React")).thenReturn(false);

        Tag reactTag = Tag.builder().id(4).name("React").createdAt(now).build();
        when(tagRepository.save(any(Tag.class))).thenReturn(reactTag);
        when(tagRepository.findAll()).thenReturn(List.of(springTag, reactTag));

        // when
        tagService.updatePostTags(post, newTagNames);

        // then
        verify(post).clearTags();
        verify(post, times(2)).addTag(any(Tag.class));
        verify(tagRepository).save(any(Tag.class)); // For creating React tag
        verify(tagRepository).findAll(); // For cleanup check
    }

    @Test
    @DisplayName("updatePostTags should handle null tag names")
    void updatePostTags_whenTagNamesIsNull_shouldClearTags() {
        // given
        Post post = mock(Post.class);
        when(tagRepository.findAll()).thenReturn(List.of());

        // when
        tagService.updatePostTags(post, null);

        // then
        verify(post).clearTags();
        verify(post, never()).addTag(any(Tag.class));
        verify(tagRepository).findAll(); // For cleanup check
    }
}