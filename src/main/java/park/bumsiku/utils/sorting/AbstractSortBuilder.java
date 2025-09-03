package park.bumsiku.utils.sorting;

public abstract class AbstractSortBuilder<T extends Enum<T>> {

    protected abstract T getDefaultField();
    protected abstract T parseField(String fieldParameter);
    protected abstract String getJpqlField(T field);
    protected abstract String getParameterName(T field);

    public SortCriteria buildSortCriteria(String sortParameter) {
        if (sortParameter == null || sortParameter.trim().isEmpty()) {
            return buildDefaultSort();
        }

        String[] parts = sortParameter.split(",");
        String fieldParam = parts[0];
        String directionParam = parts.length > 1 ? parts[1] : "desc";

        T field = parseField(fieldParam);
        SortDirection direction = SortDirection.fromParameter(directionParam);

        String jpqlClause = "ORDER BY " + getJpqlField(field) + " " + direction.getJpqlDirection();

        return new SortCriteria(getParameterName(field), direction.getJpqlDirection(), jpqlClause);
    }

    protected SortCriteria buildDefaultSort() {
        T defaultField = getDefaultField();
        SortDirection defaultDirection = SortDirection.DESC;
        String jpqlClause = "ORDER BY " + getJpqlField(defaultField) + " " + defaultDirection.getJpqlDirection();
        return new SortCriteria(getParameterName(defaultField), defaultDirection.getJpqlDirection(), jpqlClause);
    }
}