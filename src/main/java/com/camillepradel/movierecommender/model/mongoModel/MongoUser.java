package com.camillepradel.movierecommender.model.mongoModel;

import java.util.ArrayList;
import java.util.List;

public class MongoUser {
    public int _id;
    public String occupation;
    public int age;
    public String gender;
    public List<MongoUserMovie> movies;

    public MongoUser() {
    }

    public MongoUser(int _id, String occupation, int age, String gender) {
        this._id = _id;
        this.occupation = occupation;
        this.age = age;
        this.gender = gender;
        this.movies = new ArrayList<>();
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<MongoUserMovie> getMovies() {
        return movies;
    }

    public void setMovies(List<MongoUserMovie> movies) {
        this.movies = movies;
    }
}
