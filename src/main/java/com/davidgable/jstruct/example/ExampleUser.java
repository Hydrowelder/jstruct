package com.davidgable.jstruct.example;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.davidgable.jstruct.BaseModel;

import jakarta.validation.constraints.Pattern;

/**
 * Represents a user entity with personal information.
 *
 * This class extends {@link BaseModel} and provides
 * serialization/deserialization capabilities to JSON format. Each user has a
 * name, birth year, favorite food, and a timestamp of when the object was
 * created.
 *
 * @param name         the user's name
 * @param birthyear    the user's birth year
 * @param favoriteFood the user's favorite food
 *
 * @author David Gable
 * @version 1.0
 */
public class ExampleUser extends BaseModel {
    private static final Logger logger = LoggerFactory.getLogger(ExampleUser.class);

    /**
     * The user's full name.
     */
    private String name;

    /**
     * The year the user was born.
     */
    private int birthyear;

    /**
     * The user's favorite food. Cannot be equal to "Revenge".
     */
    @Pattern(regexp = "^(?!Revenge$).*$", message = "favoriteFood must not be 'Revenge'")
    private String favoriteFood;

    /**
     * The timestamp when this ExampleUser object was created.
     */
    private Instant generatedTime;

    public ExampleUser() {
    }

    public Instant getGeneratedTime() {
        return generatedTime;
    }

    public void setGeneratedTime(Instant generatedTime) {
        this.generatedTime = generatedTime;
    }

    public String getFavoriteFood() {
        return favoriteFood;
    }

    public void setFavoriteFood(String favoriteFood) {
        this.favoriteFood = favoriteFood;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBirthyear() {
        return birthyear;
    }

    public void setBirthyear(int age) {
        this.birthyear = age;
    }

    public ExampleUser(String name, int birthyear, String favoriteFood) {
        this.name = name;
        this.birthyear = birthyear;
        this.favoriteFood = favoriteFood;
        this.generatedTime = Instant.now();

        // include this for model validation!
        this.modelValidate();
    }

    public static void main(String[] args) throws Exception {
        String filename = "user.json";

        // serialize a user
        {
            ExampleUser user = new ExampleUser("Arthur", 1863, "Bear");
            user.modelDumpWrite(filename, 4); // write to JSON

            logger.info("Successfully serialized to JSON: {}", user.modelDumpJson(4));
        }

        // deserialize the example entry from the saved file
        {
            ExampleUser user = new ExampleUser();
            ExampleUser newUser = user.modelValidate(filename, ExampleUser.class); // load from JSON

            logger.info("Successfully deserialized from JSON: {}", newUser.modelDumpJson(4));
        }

        // this one fails validation since Revenge is not a valid favoriteFood
        try {
            ExampleUser badUser = new ExampleUser("Micah", 1860, "Revenge");
            logger.info("Somehow {} returned.", badUser.getName());
        } catch (IllegalStateException e) {
            logger.info("Micah has been stopped in his tracks (this is a good thing!) {}", e);
        }
    }
}
