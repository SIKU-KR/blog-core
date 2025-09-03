package park.bumsiku.utils.sorting;

import lombok.Getter;

@Getter
public enum PostSortField {
    VIEWS("views", "p.views"),
    CREATED_AT("createdAt", "p.createdAt");

    private final String parameterName;
    private final String jpqlField;

    PostSortField(String parameterName, String jpqlField) {
        this.parameterName = parameterName;
        this.jpqlField = jpqlField;
    }

    public static PostSortField fromParameter(String parameter) {
        String normalized = parameter.trim().toLowerCase();
        for (PostSortField field : values()) {
            if (field.getParameterName().equalsIgnoreCase(normalized) ||
                    field.getParameterName().replace("A", "_a").equalsIgnoreCase(normalized)) {
                return field;
            }
        }
        return CREATED_AT; // 기본값
    }
}