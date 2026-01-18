package com.davidgable.jstruct;

/**
 * Base calss for all JStruct data models.
 * <p>
 * {@code BaseModel} provides common functionality for structured data objects,
 * including standardized string representation and JSON serialization support.
 * User-defined models should extend this class to inherit default behavior and
 * integration with the JStruct serialization pipeline.
 * </p>
 *
 * @author David Gable
 * @version 1.0
 */
public abstract class BaseModel extends Serializable {
    @Override
    public String toString() {
        try {
            return this.getClass().getSimpleName() + this.modelDumpJson(0);
        } catch (Exception e) {
            return this.getClass().getSimpleName() + '{' + e + '}';
        }
    }
}
