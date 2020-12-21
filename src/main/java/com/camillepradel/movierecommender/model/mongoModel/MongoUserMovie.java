package com.camillepradel.movierecommender.model.mongoModel;

public class MongoUserMovie {
    public int movieid;
    public float rating;
    public int timestamp;

    public MongoUserMovie() {
    }

    public MongoUserMovie(int movieid, float rating, int timestamp) {
        this.movieid = movieid;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public int getMovieid() {
        return movieid;
    }

    public void setMovieid(int movieid) {
        this.movieid = movieid;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
