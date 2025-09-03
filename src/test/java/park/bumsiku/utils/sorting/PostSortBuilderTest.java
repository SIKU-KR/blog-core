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
        SortCriteria result = postSortBuilder.buildSortCriteria("views,desc");

        // then
        assertThat(result.field()).isEqualTo("views");
        assertThat(result.direction()).isEqualTo("DESC");
        assertThat(result.jpqlOrderClause()).isEqualTo("ORDER BY p.views DESC");
    }

    @Test
    public void shouldReturnViewsAscSort() {
        // when
        SortCriteria result = postSortBuilder.buildSortCriteria("views,asc");

        // then
        assertThat(result.field()).isEqualTo("views");
        assertThat(result.direction()).isEqualTo("ASC");
        assertThat(result.jpqlOrderClause()).isEqualTo("ORDER BY p.views ASC");
    }

    @Test
    public void shouldReturnCreatedAtDescSort() {
        // when
        SortCriteria result = postSortBuilder.buildSortCriteria("createdAt,desc");

        // then
        assertThat(result.field()).isEqualTo("createdAt");
        assertThat(result.direction()).isEqualTo("DESC");
        assertThat(result.jpqlOrderClause()).isEqualTo("ORDER BY p.createdAt DESC");
    }

    @Test
    public void shouldReturnCreatedAtAscSort() {
        // when
        SortCriteria result = postSortBuilder.buildSortCriteria("createdAt,asc");

        // then
        assertThat(result.field()).isEqualTo("createdAt");
        assertThat(result.direction()).isEqualTo("ASC");
        assertThat(result.jpqlOrderClause()).isEqualTo("ORDER BY p.createdAt ASC");
    }

    @Test
    public void shouldReturnDefaultSortForNullInput() {
        // when
        SortCriteria result = postSortBuilder.buildSortCriteria(null);

        // then
        assertThat(result.field()).isEqualTo("createdAt");
        assertThat(result.direction()).isEqualTo("DESC");
        assertThat(result.jpqlOrderClause()).isEqualTo("ORDER BY p.createdAt DESC");
    }

    @Test
    public void shouldReturnDefaultSortForEmptyInput() {
        // when
        SortCriteria result = postSortBuilder.buildSortCriteria("");

        // then
        assertThat(result.field()).isEqualTo("createdAt");
        assertThat(result.direction()).isEqualTo("DESC");
        assertThat(result.jpqlOrderClause()).isEqualTo("ORDER BY p.createdAt DESC");
    }

    @Test
    public void shouldReturnDefaultSortForInvalidField() {
        // when
        SortCriteria result = postSortBuilder.buildSortCriteria("invalidField,desc");

        // then
        assertThat(result.field()).isEqualTo("createdAt");
        assertThat(result.direction()).isEqualTo("DESC");
        assertThat(result.jpqlOrderClause()).isEqualTo("ORDER BY p.createdAt DESC");
    }

    @Test
    public void shouldUseDescAsDefaultDirection() {
        // when
        SortCriteria result = postSortBuilder.buildSortCriteria("views");

        // then
        assertThat(result.field()).isEqualTo("views");
        assertThat(result.direction()).isEqualTo("DESC");
        assertThat(result.jpqlOrderClause()).isEqualTo("ORDER BY p.views DESC");
    }

    @Test
    public void shouldReturnDescForInvalidDirection() {
        // when
        SortCriteria result = postSortBuilder.buildSortCriteria("views,invalid");

        // then
        assertThat(result.field()).isEqualTo("views");
        assertThat(result.direction()).isEqualTo("DESC");
        assertThat(result.jpqlOrderClause()).isEqualTo("ORDER BY p.views DESC");
    }

    @Test
    public void shouldBeCaseInsensitive() {
        // when
        SortCriteria result = postSortBuilder.buildSortCriteria("VIEWS,ASC");

        // then
        assertThat(result.field()).isEqualTo("views");
        assertThat(result.direction()).isEqualTo("ASC");
        assertThat(result.jpqlOrderClause()).isEqualTo("ORDER BY p.views ASC");
    }
}