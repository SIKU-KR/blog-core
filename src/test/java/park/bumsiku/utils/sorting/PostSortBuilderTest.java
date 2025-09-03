package park.bumsiku.utils.sorting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PostSortBuilderTest {

    @InjectMocks
    private PostSortBuilder postSortBuilder;

    @Test
    public void shouldReturnViewsDescSort() {
        // when
        PostSortBuilder.SortCriteria result = postSortBuilder.buildSortCriteria("views,desc");

        // then
        assertThat(result.getField()).isEqualTo("views");
        assertThat(result.getDirection()).isEqualTo("DESC");
        assertThat(result.getJpqlOrderClause()).isEqualTo("ORDER BY p.views DESC");
    }

    @Test
    public void shouldReturnViewsAscSort() {
        // when
        PostSortBuilder.SortCriteria result = postSortBuilder.buildSortCriteria("views,asc");

        // then
        assertThat(result.getField()).isEqualTo("views");
        assertThat(result.getDirection()).isEqualTo("ASC");
        assertThat(result.getJpqlOrderClause()).isEqualTo("ORDER BY p.views ASC");
    }

    @Test
    public void shouldReturnCreatedAtDescSort() {
        // when
        PostSortBuilder.SortCriteria result = postSortBuilder.buildSortCriteria("createdAt,desc");

        // then
        assertThat(result.getField()).isEqualTo("createdAt");
        assertThat(result.getDirection()).isEqualTo("DESC");
        assertThat(result.getJpqlOrderClause()).isEqualTo("ORDER BY p.createdAt DESC");
    }

    @Test
    public void shouldReturnCreatedAtAscSort() {
        // when
        PostSortBuilder.SortCriteria result = postSortBuilder.buildSortCriteria("createdAt,asc");

        // then
        assertThat(result.getField()).isEqualTo("createdAt");
        assertThat(result.getDirection()).isEqualTo("ASC");
        assertThat(result.getJpqlOrderClause()).isEqualTo("ORDER BY p.createdAt ASC");
    }

    @Test
    public void shouldReturnDefaultSortForNullInput() {
        // when
        PostSortBuilder.SortCriteria result = postSortBuilder.buildSortCriteria(null);

        // then
        assertThat(result.getField()).isEqualTo("createdAt");
        assertThat(result.getDirection()).isEqualTo("DESC");
        assertThat(result.getJpqlOrderClause()).isEqualTo("ORDER BY p.createdAt DESC");
    }

    @Test
    public void shouldReturnDefaultSortForEmptyInput() {
        // when
        PostSortBuilder.SortCriteria result = postSortBuilder.buildSortCriteria("");

        // then
        assertThat(result.getField()).isEqualTo("createdAt");
        assertThat(result.getDirection()).isEqualTo("DESC");
        assertThat(result.getJpqlOrderClause()).isEqualTo("ORDER BY p.createdAt DESC");
    }

    @Test
    public void shouldReturnDefaultSortForInvalidField() {
        // when
        PostSortBuilder.SortCriteria result = postSortBuilder.buildSortCriteria("invalidField,desc");

        // then
        assertThat(result.getField()).isEqualTo("createdAt");
        assertThat(result.getDirection()).isEqualTo("DESC");
        assertThat(result.getJpqlOrderClause()).isEqualTo("ORDER BY p.createdAt DESC");
    }

    @Test
    public void shouldUseDescAsDefaultDirection() {
        // when
        PostSortBuilder.SortCriteria result = postSortBuilder.buildSortCriteria("views");

        // then
        assertThat(result.getField()).isEqualTo("views");
        assertThat(result.getDirection()).isEqualTo("DESC");
        assertThat(result.getJpqlOrderClause()).isEqualTo("ORDER BY p.views DESC");
    }

    @Test
    public void shouldReturnDescForInvalidDirection() {
        // when
        PostSortBuilder.SortCriteria result = postSortBuilder.buildSortCriteria("views,invalid");

        // then
        assertThat(result.getField()).isEqualTo("views");
        assertThat(result.getDirection()).isEqualTo("DESC");
        assertThat(result.getJpqlOrderClause()).isEqualTo("ORDER BY p.views DESC");
    }

    @Test
    public void shouldBeCaseInsensitive() {
        // when
        PostSortBuilder.SortCriteria result = postSortBuilder.buildSortCriteria("VIEWS,ASC");

        // then
        assertThat(result.getField()).isEqualTo("views");
        assertThat(result.getDirection()).isEqualTo("ASC");
        assertThat(result.getJpqlOrderClause()).isEqualTo("ORDER BY p.views ASC");
    }
}