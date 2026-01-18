package com.davidgable.jstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Base class for all JStruct data models.
 *
 * <p>
 * {@code BaseModel} provides common functionality for structured data objects,
 * including standardized string representation and JSON serialization support.
 * User-defined models should extend this class to inherit default behavior and
 * integration with the JStruct serialization pipeline.
 * </p>
 *
 * <p>
 * It comes with serialization/deserialization (using Jackson) and data
 * validation (using Jakarta). When using deserializing a model,
 * BaseModel.modelValidate() is run to validate data fields to check for
 * constraint violations.
 * </p>
 *
 * @author David Gable
 * @version 1.0
 */
public abstract class BaseModel {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Validates data fields to check for constraint violations.
     *
     * @throws IllegalStateException if the validation fails for any reason.
     */
    public void modelValidate() throws IllegalStateException {

        Set<ConstraintViolation<BaseModel>> violations = validator.validate(this);

        if (!violations.isEmpty()) {

            for (ConstraintViolation<BaseModel> violation : violations) {
                logger.error("{} -> {}",
                        violation.getPropertyPath(),
                        violation.getMessage());
            }

            throw new IllegalStateException(
                    "Validation failed for " + getClass().getName());
        }
    }

    /**
     * Resolves the filepath to a file
     *
     * @param filename      a {@code Path} object representing the Path to the
     *                      file
     * @param fileMustExist a {@code boolean} which specifies if the path needs to
     *                      exist or not
     * @return a {@code Path} object which was resolved (and validated if it needs
     *         to exist)
     * @throws IOException if the file did not exist (and needed to) or if the path
     *                     was unable to be resolved.
     */
    private Path resolveFilepath(Path filename, boolean fileMustExist) throws IOException {
        // convert to an absolute path regardless if the file exists or not
        Path path = filename.toAbsolutePath();

        // warn the user if the path might be invalid
        if (!path.toString().endsWith(".json")) {
            logger.warn("The filename specified ({}) does not end with .json", path);
        }
        if (!fileMustExist) {
            return path;
        }

        // path validation
        try {
            path = path.toRealPath();

            // path must be a file specifically
            if (!Files.isRegularFile(path)) {
                throw new IOException("Filepath (" + path + ") is not a file!");
            }
            return path;
        } catch (IOException e) {
            throw new IOException("Unexpected error resolving filepath " + filename, e);
        }
    }

    /**
     * Gets an object mapper for serialization and deserialization
     *
     * @return a new {@code ObjectMapper}
     */
    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    /**
     * Get a JSON string for the object
     *
     * @param indent an {@code int} that sets how many spaces to indent with (adds
     *               newlines and spaces if greater than to 0)
     * @return a {@code String} with indents (if specified)
     * @throws JsonProcessingException if unable to write the object to a JSON
     *                                 string
     */
    public String modelDumpJson(int indent) throws JsonProcessingException {
        ObjectMapper objectMapper = this.getObjectMapper();
        if (indent <= 0) {
            return objectMapper.writeValueAsString(this);
        }

        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(this)
                .replaceAll("  ", " ".repeat(indent));
    }

    /**
     * Serializes an object to a filename
     *
     * @param filename a {@code String} for where the object should be serialized
     * @param indent   an {@code int} that sets how many spaces to
     *                 indent with (adds newlines and spaces if greater than to 0)
     * @return a {@code boolean} specifying if serialization was successful
     */
    public boolean modelDumpWrite(String filename, int indent) {
        return this.modelDumpWrite(Paths.get(filename), indent);
    }

    /**
     * Serializes an object to a filename
     *
     * @param filename a {@code Path} for where the object should be serialized
     * @param indent   an {@code int} that sets how many spaces to
     *                 indent with (adds newlines and spaces if greater than to 0)
     * @return a {@code boolean} specifying if serialization was successful
     */
    public boolean modelDumpWrite(Path filename, int indent) {
        Path path;
        try {
            path = this.resolveFilepath(filename, false);
        } catch (IOException e) {
            return false;
        }
        logger.debug("Serializing to: {}", path);

        String jsonString;
        try {
            jsonString = this.modelDumpJson(indent);
        } catch (Exception e) {
            logger.error("Unable to serialize to JSON with error {}", e);
            return false;
        }

        // write the string to a file
        try {
            Files.writeString(path, jsonString);
            logger.debug("Serialized to JSON at {}", path);
        } catch (Exception e) {
            logger.error("Failed to write JSON file: {}", path, e);
            return false;
        }

        return true;
    }

    /**
     * Deserializes a JSON file into an object of the specified type from a Path
     * string. Validates data fields to check for constraint violations.
     *
     * @param <T>      the type to deserialize into
     * @param filename a {@code String} object representing the path to the JSON
     *                 file
     * @param type     a {@code Class<T>} object representing the type to
     *                 deserialize into
     * @return an object of type {@code T} deserialized from the JSON file
     * @throws IOException           if the file does not exist, cannot be read, or
     *                               deserialization fails
     * @throws IllegalStateException if the validation fails for any reason.
     */
    public <T extends BaseModel> T modelValidate(String filename, Class<T> type) throws IOException {
        return this.modelValidate(Paths.get(filename), type);
    }

    /**
     * Deserializes a JSON file into an object of the specified type from a Path.
     * Validates data fields to check for constraint violations.
     *
     * @param <T>      the type to deserialize into
     * @param filename a {@code Path} object representing the path to the JSON file
     * @param type     a {@code Class<T>} object representing the type to
     *                 deserialize into
     * @return an object of type {@code T} deserialized from the JSON file
     * @throws IOException           if the file does not exist, cannot be read, or
     *                               deserialization fails
     * @throws IllegalStateException if the validation fails for any reason.
     */
    public <T extends BaseModel> T modelValidate(Path filename, Class<T> type) throws IOException {
        Path path = this.resolveFilepath(filename, true);

        String jsonString = Files.readString(path);
        return this.modelValidateJson(jsonString, type);
    }

    /**
     * Deserializes a JSON file into an object of the specified type from a string.
     * Validates data fields to check for constraint violations.
     *
     * @param <T>        the type to deserialize into
     * @param jsonString a {@code String} object representing the content of a JSON
     *                   file
     * @param type       a {@code Class<T>} object representing the type to
     *                   deserialize into
     * @return an object of type {@code T} deserialized from the JSON file
     * @throws IOException           if the file does not exist, cannot be read, or
     *                               deserialization fails
     * @throws IllegalStateException if the validation fails for any reason.
     */
    public <T extends BaseModel> T modelValidateJson(String jsonString, Class<T> type) throws IOException {

        ObjectMapper objectMapper = this.getObjectMapper();
        T newObj = objectMapper.readValue(jsonString, type);
        newObj.modelValidate();
        return newObj;
    }
}
