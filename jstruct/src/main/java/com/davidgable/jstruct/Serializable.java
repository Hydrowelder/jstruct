package com.davidgable.jstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Serializer/Deserialize objects.
 *
 * <p>
 * {@code Serializable} provides a method to serialize and deserialize an object
 * which inherits it. This allows for easier communication between APIs by using
 * JStruct as an inbetween.
 * </p>
 *
 * @author David Gable
 * @version 1.0
 */
public abstract class Serializable {
    private static final Logger logger = LoggerFactory.getLogger(Serializable.class);

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
            logger.warn("The filename specified (" + path + ") does not end with .json");
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
    public boolean modelDump(String filename, int indent) {
        return this.modelDump(Paths.get(filename), indent);
    }

    /**
     * Serializes an object to a filename
     *
     * @param filename a {@code Path} for where the object should be serialized
     * @param indent   an {@code int} that sets how many spaces to
     *                 indent with (adds newlines and spaces if greater than to 0)
     * @return a {@code boolean} specifying if serialization was successful
     */
    public boolean modelDump(Path filename, int indent) {
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
     * string.
     *
     * @param <T>      the type to deserialize into
     * @param filename a {@code String} object representing the path to the JSON
     *                 file
     * @param type     a {@code Class<T>} object representing the type to
     *                 deserialize into
     * @return an object of type {@code T} deserialized from the JSON file
     * @throws IOException if the file does not exist, cannot be read, or
     *                     deserialization fails
     */
    public <T> T modelValidate(String filename, Class<T> type) throws IOException {
        return this.modelValidate(Paths.get(filename), type);
    }

    /**
     * Deserializes a JSON file into an object of the specified type from a Path.
     *
     * @param <T>      the type to deserialize into
     * @param filename a {@code Path} object representing the path to the JSON file
     * @param type     a {@code Class<T>} object representing the type to
     *                 deserialize into
     * @return an object of type {@code T} deserialized from the JSON file
     * @throws IOException if the file does not exist, cannot be read, or
     *                     deserialization fails
     */
    public <T> T modelValidate(Path filename, Class<T> type) throws IOException {
        Path path = this.resolveFilepath(filename, true);

        String jsonString = Files.readString(path);
        return this.modelValidateJson(jsonString, type);
    }

    /**
     * Deserializes a JSON file into an object of the specified type from a string.
     *
     * @param <T>        the type to deserialize into
     * @param jsonString a {@code String} object representing the content of a JSON
     *                   file
     * @param type       a {@code Class<T>} object representing the type to
     *                   deserialize into
     * @return an object of type {@code T} deserialized from the JSON file
     * @throws IOException if the file does not exist, cannot be read, or
     *                     deserialization fails
     */
    public <T> T modelValidateJson(String jsonString, Class<T> type) throws IOException {

        ObjectMapper objectMapper = this.getObjectMapper();
        return objectMapper.readValue(jsonString, type);
    }
}
