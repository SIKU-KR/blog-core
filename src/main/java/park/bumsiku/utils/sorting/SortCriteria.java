package park.bumsiku.utils.sorting;

import lombok.Getter;

@Getter
public class SortCriteria {

    private final String field;
    private final String direction;
    private final String jpqlClause;

    public SortCriteria(String field, String direction, String jpqlClause) {
        this.field = field;
        this.direction = direction;
        this.jpqlClause = jpqlClause;
    }

    public String getJpqlOrderClause() {
        return jpqlClause;
    }
}