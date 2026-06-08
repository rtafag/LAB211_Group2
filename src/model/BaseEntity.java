package model;

import java.io.Serializable;

public abstract class BaseEntity implements Serializable {

    protected String id;

    public BaseEntity() {
    }

    public BaseEntity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Chuyển object thành dòng CSV
     */
    public abstract String toCsvLine();

    @Override
    public String toString() {
        return "BaseEntity{"
                + "id='" + id + '\''
                + '}';
    }
}
