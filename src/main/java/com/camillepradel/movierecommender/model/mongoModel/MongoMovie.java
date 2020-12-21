package com.camillepradel.movierecommender.model.mongoModel;

import com.camillepradel.movierecommender.model.Genre;
import com.camillepradel.movierecommender.model.Movie;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MongoMovie {
    public int _id;
    public String title;
    public String genres;

    public MongoMovie() {
    }

    public MongoMovie(int _id, String title, String genres) {
        this._id = _id;
        this.title = title;
        this.genres = genres;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public Movie convertToMovie() {
        return new Movie(this._id, this.title, Arrays.stream(this.genres.split("\\|")).map(genre -> new Genre(1, genre)).collect(Collectors.toList()));
    }
}
