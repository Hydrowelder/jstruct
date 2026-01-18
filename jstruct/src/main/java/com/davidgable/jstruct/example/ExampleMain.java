package com.davidgable.jstruct.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.davidgable.jstruct.BaseModel;

/**
 * Represents a collection of user entities.
 *
 * This class extends {@link BaseModel} and provides
 * serialization/deserialization capabilities to JSON format.
 *
 * @param users a list of users to serialize and deserialize
 *
 * @author David Gable
 * @version 1.0
 */
public class ExampleMain extends BaseModel {
    private ExampleUser[] users;
    private static final Logger logger = LoggerFactory.getLogger(ExampleMain.class);

    public ExampleMain() {
    }

    public ExampleUser[] getUsers() {
        return users;
    }

    public void setUsers(ExampleUser[] users) {
        this.users = users;
    }

    public ExampleMain(ExampleUser[] users) {
        this.users = users;
    }

    public static void main(String[] args) throws Exception {
        // make the family (note that ExampleUser is also extended by BaseModel)
        ExampleMain users = new ExampleMain(new ExampleUser[] {
                new ExampleUser("John", 1873, "Stew?"),
                new ExampleUser("Abigail", 1877, "Brains"),
                new ExampleUser("Jack", 1895, "Also Brains? Fish?"),
        });

        // serialize to json
        String filename = "users.json";
        users.modelDump(filename, 4);

        // deserialize the example entry from the saved file
        ExampleMain main = new ExampleMain();
        ExampleMain newUsers = main.modelValidate(filename, ExampleMain.class);

        // System.out.println(newUsers.modelDumpJson(4));
        logger.info(newUsers.modelDumpJson(4));
    }
}
