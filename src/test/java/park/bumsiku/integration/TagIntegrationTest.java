package park.bumsiku.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import park.bumsiku.config.AbstractTestSupport;
import park.bumsiku.domain.dto.request.CreateTagRequest;
import park.bumsiku.domain.dto.request.UpdateTagRequest;
import park.bumsiku.domain.entity.Tag;
import park.bumsiku.repository.TagRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TagIntegrationTest extends AbstractTestSupport {

    @Autowired
    private TagRepository tagRepository;

    private Tag springTag;
    private Tag javaTag;

    @BeforeEach
    void setUp() {
        springTag = tagRepository.save(Tag.builder().name("Spring").build());
        javaTag = tagRepository.save(Tag.builder().name("Java").build());
    }

    @Test
    @DisplayName("GET /admin/tags - should return all tags")
    void getAllTags_shouldReturnAllTags() throws Exception {
        mockMvc.perform(get("/admin/tags")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name", anyOf(is("Spring"), is("Java"))))
                .andExpect(jsonPath("$.data[1].name", anyOf(is("Spring"), is("Java"))));
    }

    @Test
    @DisplayName("GET /admin/tags/{tagId} - should return specific tag when tag exists")
    void getTagById_whenTagExists_shouldReturnTag() throws Exception {
        mockMvc.perform(get("/admin/tags/{tagId}", springTag.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(springTag.getId()))
                .andExpect(jsonPath("$.data.name").value("Spring"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("GET /admin/tags/{tagId} - should return 404 when tag does not exist")
    void getTagById_whenTagDoesNotExist_shouldReturn404() throws Exception {
        mockMvc.perform(get("/admin/tags/{tagId}", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message", containsString("Tag not found with id: 999")));
    }

    @Test
    @DisplayName("POST /admin/tags - should create new tag when request is valid")
    void createTag_whenRequestIsValid_shouldCreateTag() throws Exception {
        CreateTagRequest request = CreateTagRequest.builder()
                .name("React")
                .build();

        mockMvc.perform(post("/admin/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("React"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("POST /admin/tags - should return 400 when tag name already exists")
    void createTag_whenTagNameAlreadyExists_shouldReturn400() throws Exception {
        CreateTagRequest request = CreateTagRequest.builder()
                .name("Spring")
                .build();

        mockMvc.perform(post("/admin/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message", containsString("Tag with name 'Spring' already exists")));
    }

    @Test
    @DisplayName("POST /admin/tags - should return 400 when tag name is invalid")
    void createTag_whenTagNameIsInvalid_shouldReturn400() throws Exception {
        CreateTagRequest request = CreateTagRequest.builder()
                .name("")
                .build();

        mockMvc.perform(post("/admin/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message", containsString("태그 이름을 입력해주세요")));
    }

    @Test
    @DisplayName("PUT /admin/tags/{tagId} - should update tag when request is valid")
    void updateTag_whenRequestIsValid_shouldUpdateTag() throws Exception {
        UpdateTagRequest request = UpdateTagRequest.builder()
                .name("Spring Framework")
                .build();

        mockMvc.perform(put("/admin/tags/{tagId}", springTag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(springTag.getId()))
                .andExpect(jsonPath("$.data.name").value("Spring Framework"));
    }

    @Test
    @DisplayName("PUT /admin/tags/{tagId} - should return 404 when tag does not exist")
    void updateTag_whenTagDoesNotExist_shouldReturn404() throws Exception {
        UpdateTagRequest request = UpdateTagRequest.builder()
                .name("NonExistent")
                .build();

        mockMvc.perform(put("/admin/tags/{tagId}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message", containsString("Tag not found with id: 999")));
    }

    @Test
    @DisplayName("PUT /admin/tags/{tagId} - should return 400 when new name already exists")
    void updateTag_whenNewNameAlreadyExists_shouldReturn400() throws Exception {
        UpdateTagRequest request = UpdateTagRequest.builder()
                .name("Java")
                .build();

        mockMvc.perform(put("/admin/tags/{tagId}", springTag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message", containsString("Tag with name 'Java' already exists")));
    }

    @Test
    @DisplayName("DELETE /admin/tags/{tagId} - should delete tag when tag exists")
    void deleteTag_whenTagExists_shouldDeleteTag() throws Exception {
        mockMvc.perform(delete("/admin/tags/{tagId}", springTag.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Tag deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /admin/tags/{tagId} - should return 404 when tag does not exist")
    void deleteTag_whenTagDoesNotExist_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/admin/tags/{tagId}", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message", containsString("Tag not found with id: 999")));
    }

    @Test
    @DisplayName("GET /tags - should return all tags for public access")
    void getTagsPublic_shouldReturnAllTags() throws Exception {
        mockMvc.perform(get("/tags")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name", anyOf(is("Spring"), is("Java"))))
                .andExpect(jsonPath("$.data[1].name", anyOf(is("Spring"), is("Java"))));
    }

    @Test
    @DisplayName("GET /admin/tags/{tagId} - should return 400 for invalid tag ID")
    void getTagById_whenTagIdIsInvalid_shouldReturn400() throws Exception {
        mockMvc.perform(get("/admin/tags/{tagId}", -1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message", containsString("태그 ID는 양수여야 합니다")));
    }

    @Test
    @DisplayName("POST /admin/tags - should trim whitespace from tag name")
    void createTag_shouldTrimWhitespaceFromName() throws Exception {
        CreateTagRequest request = CreateTagRequest.builder()
                .name("  Vue.js  ")
                .build();

        mockMvc.perform(post("/admin/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Vue.js"));
    }

    @Test
    @DisplayName("PUT /admin/tags/{tagId} - should allow updating to same name (case insensitive)")
    void updateTag_whenUpdatingToSameNameWithDifferentCase_shouldSucceed() throws Exception {
        UpdateTagRequest request = UpdateTagRequest.builder()
                .name("SPRING")
                .build();

        mockMvc.perform(put("/admin/tags/{tagId}", springTag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("SPRING"));
    }
}