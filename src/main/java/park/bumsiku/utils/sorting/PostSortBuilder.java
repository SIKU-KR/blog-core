package park.bumsiku.utils.sorting;

import org.springframework.stereotype.Component;

@Component
public class PostSortBuilder {

    public static class SortCriteria {
        private final String field;
        private final String direction;
        private final String jpqlClause;

        public SortCriteria(String field, String direction, String jpqlClause) {
            this.field = field;
            this.direction = direction;
            this.jpqlClause = jpqlClause;
        }

        public String getField() {
            return field;
        }

        public String getDirection() {
            return direction;
        }

        public String getJpqlOrderClause() {
            return jpqlClause;
        }
    }

    public enum SortField {
        VIEWS("views", "p.views"),
        CREATED_AT("createdAt", "p.createdAt");

        private final String parameterName;
        private final String jpqlField;

        SortField(String parameterName, String jpqlField) {
            this.parameterName = parameterName;
            this.jpqlField = jpqlField;
        }

        public String getParameterName() {
            return parameterName;
        }

        public String getJpqlField() {
            return jpqlField;
        }

        public static SortField fromParameter(String parameter) {
            String normalized = parameter.trim().toLowerCase();
            for (SortField field : values()) {
                if (field.getParameterName().equalsIgnoreCase(normalized) ||
                        field.getParameterName().replace("A", "_a").equalsIgnoreCase(normalized)) {
                    return field;
                }
            }
            return CREATED_AT; // 기본값
        }
    }

    public enum SortDirection {
        ASC("ASC"), DESC("DESC");

        private final String jpqlDirection;

        SortDirection(String jpqlDirection) {
            this.jpqlDirection = jpqlDirection;
        }

        public String getJpqlDirection() {
            return jpqlDirection;
        }

        public static SortDirection fromParameter(String parameter) {
            if (parameter == null || parameter.trim().isEmpty()) {
                return DESC;
            }
            try {
                return SortDirection.valueOf(parameter.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return DESC; // 기본값
            }
        }
    }

    public SortCriteria buildSortCriteria(String sortParameter) {
        if (sortParameter == null || sortParameter.trim().isEmpty()) {
            return getDefaultSort();
        }

        String[] parts = sortParameter.split(",");
        String fieldParam = parts[0];
        String directionParam = parts.length > 1 ? parts[1] : "desc";

        SortField field = SortField.fromParameter(fieldParam);
        SortDirection direction = SortDirection.fromParameter(directionParam);

        String jpqlClause = "ORDER BY " + field.getJpqlField() + " " + direction.getJpqlDirection();

        return new SortCriteria(field.getParameterName(), direction.getJpqlDirection(), jpqlClause);
    }

    public SortCriteria getDefaultSort() {
        return new SortCriteria("createdAt", "DESC", "ORDER BY p.createdAt DESC");
    }
}