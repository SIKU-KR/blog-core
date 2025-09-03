package park.bumsiku.utils.sorting;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortDirection {
    ASC("ASC"),
    DESC("DESC");

    private final String jpqlDirection;

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