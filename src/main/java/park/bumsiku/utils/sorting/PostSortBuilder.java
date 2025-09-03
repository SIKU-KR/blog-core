package park.bumsiku.utils.sorting;

import org.springframework.stereotype.Component;

@Component
public class PostSortBuilder {

    public SortCriteria buildSortCriteria(String sortParameter) {
        if (sortParameter == null || sortParameter.trim().isEmpty()) {
            return getDefaultSort();
        }

        String[] parts = sortParameter.split(",");
        String fieldParam = parts[0];
        String directionParam = parts.length > 1 ? parts[1] : "desc";

        PostSortField field = PostSortField.fromParameter(fieldParam);
        SortDirection direction = SortDirection.fromParameter(directionParam);

        String jpqlClause = "ORDER BY " + field.getJpqlField() + " " + direction.getJpqlDirection();

        return new SortCriteria(field.getParameterName(), direction.getJpqlDirection(), jpqlClause);
    }

    public SortCriteria getDefaultSort() {
        return new SortCriteria("createdAt", "DESC", "ORDER BY p.createdAt DESC");
    }
}