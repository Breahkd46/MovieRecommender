package com.camillepradel.movierecommender.model;

public class Genre {

    private int id;
    private String name;

    public Genre() {
        this.id = 0;
        this.name = "";
    }

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Genre(String name) {
        this.id = 0;
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
