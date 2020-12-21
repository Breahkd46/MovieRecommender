package com.camillepradel.movierecommender.utils;

import com.camillepradel.movierecommender.model.mongoModel.MongoMovie;
import com.camillepradel.movierecommender.model.mongoModel.MongoUser;
import com.camillepradel.movierecommender.model.mongoModel.MongoUserMovie;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class CsvToMongo {

    static final String pathToCsvFiles = "/home/valo/IdeaProjects/MovieRecommender/src/main/java/com/camillepradel/movierecommender/utils/";
    static final String usersCsvFile = pathToCsvFiles + "users.csv";
    static final String moviesCsvFile = pathToCsvFiles + "movies.csv";
    static final String genresCsvFile = pathToCsvFiles + "genres.csv";
    static final String movGenreCsvFile = pathToCsvFiles + "mov_genre.csv";
    static final String ratingsCsvFile = pathToCsvFiles + "ratings.csv";
    static final String friendsCsvFile = pathToCsvFiles + "friends.csv";
    static final String cvsSplitBy = ",";

    private static void commitMovie(MongoDatabase database) {
        System.out.println(moviesCsvFile);

        String collectionName = "movies";

        // create table
        database.getCollection(collectionName).drop();

        database.createCollection(collectionName);

        MongoCollection<MongoMovie> movies = database.getCollection(collectionName, MongoMovie.class);

        Map<Integer, MongoMovie> mongoMovieMap = new HashMap<>();

        // populate table
        try (BufferedReader br = new BufferedReader(new FileReader(moviesCsvFile))) {
            String line;
            br.readLine(); // skip first line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(cvsSplitBy);
                mongoMovieMap.put(Integer.parseInt(values[0]), new MongoMovie(Integer.parseInt(values[0]), values[1], ""));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<Integer, String> genres = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(genresCsvFile))) {
            String line;
            br.readLine(); // skip first line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(cvsSplitBy);
                genres.put(Integer.parseInt(values[1]), values[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        try (BufferedReader br = new BufferedReader(new FileReader(movGenreCsvFile))) {
            String line;
            br.readLine(); // skip first line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(cvsSplitBy);

                try {
                    MongoMovie mongoMovie = mongoMovieMap.get(Integer.parseInt(values[0]));

                    mongoMovie.setGenres((mongoMovie.genres.equals("")) ?
                            genres.get(Integer.parseInt(values[1])) :
                            mongoMovie.genres + "|" + genres.get(Integer.parseInt(values[1])));

                } catch (IndexOutOfBoundsException e ) {
                    System.out.println("Movies not handled : " + values[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        movies.insertMany(new ArrayList<>(mongoMovieMap.values()));

    }

    private static void commitUsers(MongoDatabase database) {
        System.out.println(usersCsvFile);

        String collectionName = "users";

        // create table
        database.getCollection(collectionName).drop();

        database.createCollection(collectionName);

        MongoCollection<MongoUser> users = database.getCollection(collectionName, MongoUser.class);

        Map<Integer, MongoUser> mongoUsers = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(usersCsvFile))) {
            String line;
            br.readLine(); // skip first line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(cvsSplitBy);
                mongoUsers.put(Integer.parseInt(values[0]), new MongoUser(Integer.parseInt(values[0]), values[3], Integer.parseInt(values[1]), values[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(ratingsCsvFile))) {
            String line;
            br.readLine(); // skip first line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(cvsSplitBy);
                try {
                    mongoUsers.get(Integer.parseInt(values[0])).movies.add(new MongoUserMovie(
                            Integer.parseInt(values[1]),
                            Integer.parseInt(values[2]),
                            Integer.parseInt(values[3])));
                } catch (IndexOutOfBoundsException e ) {
                    System.out.println("User not handled : " + values[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        users.insertMany(new ArrayList<>(mongoUsers.values()));
    }



    public static void main(String[] args) {
        // load JDBC driver

        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://root:root@localhost:27017/?authSource=admin"));
        MongoDatabase database = mongoClient.getDatabase("MovieLens").withCodecRegistry(pojoCodecRegistry);

        commitMovie(database);
        commitUsers(database);

        System.out.println("done");
    }
}
