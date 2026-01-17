package com.davidgable.jstruct.example;

import java.time.Instant;

import com.davidgable.jstruct.*;

public class ExampleMain extends BaseModel {
    private String name;
    private int age;
    private String favoriteFood;
    private Instant generatedTime;

    public ExampleMain() {
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public ExampleMain(String name, int age, String favoriteFood) {
        this.setName(name);
        this.setAge(age);
        this.setFavoriteFood(favoriteFood);
        this.setGeneratedTime(Instant.now());
    }

    public static void main(String[] args) throws Exception {
        // make an example entry
        ExampleMain john = new ExampleMain("john", 10, "Chicken");

        // serialize the example entry
        String filename = "user.json";
        john.toJsonFile(filename, 4);

        // deserialize the example entry from the saved file
        ExampleMain user = new ExampleMain();
        ExampleMain newUser = user.fromJsonFile(filename, ExampleMain.class);

        System.out.println(newUser);
    }
}
