package com.camillepradel.movierecommender.model.mongoModel;

public class MongoUserMovie {
    public int movieid;
    public int rating;
    public int timestamp;

    public MongoUserMovie() {
    }

    public MongoUserMovie(int movieid, int rating, int timestamp) {
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
