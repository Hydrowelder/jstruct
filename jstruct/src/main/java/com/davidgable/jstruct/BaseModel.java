package com.davidgable.jstruct;

public abstract class BaseModel extends Serializable {
    @Override
    public String toString() {
        try {
            return this.getClass().getSimpleName() + this.jsonString(0);
        } catch (Exception e) {
            return this.getClass().getSimpleName() + '{' + e + '}';
        }
    }
}
