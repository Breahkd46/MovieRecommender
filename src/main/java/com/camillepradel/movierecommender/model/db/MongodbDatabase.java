package com.camillepradel.movierecommender.model.db;

import com.camillepradel.movierecommender.model.Genre;
import com.camillepradel.movierecommender.model.mongoModel.MongoMovie;
import com.camillepradel.movierecommender.model.Movie;
import com.camillepradel.movierecommender.model.Rating;
import com.camillepradel.movierecommender.model.mongoModel.MongoUserMovie;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.Arrays;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.descending;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongodbDatabase extends AbstractDatabase {

    private final MongoDatabase database;
    static String MOVIES = "movies";
    static String USERS = "users";
    static String USERNAME = "root";
    static String PASSWORD = "root";
    static String DATABASE = "admin";

    /**
     *
     */
    public MongodbDatabase() {
        /*
         * CodecRegistry allows Custmom POJO
         *          - MongoMovie
         *          - MongoUser
         */
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));


        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://root:root@localhost:27017/?authSource=admin"));
        this.database = mongoClient.getDatabase("MovieLens").withCodecRegistry(pojoCodecRegistry);
    }

    @Override
    public List<Movie> getAllMovies() {
        // TODO: write query to retrieve all movies from DB
        return StreamSupport.stream(
                this.database.getCollection(MOVIES).find(new BasicDBObject(), MongoMovie.class).spliterator(),
                false).map(MongoMovie::convertToMovie).collect(Collectors.toList());
    }

    @Override
    public List<Movie> getMoviesRatedByUser(int userId) {
        // TODO: write query to retrieve all movies rated by user with id userId

        return StreamSupport.stream(this.database.getCollection(USERS).aggregate(Arrays.asList(
                match(Filters.eq("_id", userId)),
                Aggregates.unwind("$movies"),
                Aggregates.project(Projections.computed("movieid", "$movies.movieid")),
                Aggregates.lookup(MOVIES, "movieid", "_id", "movie"),
                Aggregates.unwind("$movie"),
                Aggregates.replaceRoot("$movie")), MongoMovie.class)
                .spliterator(), false)
                .map(MongoMovie::convertToMovie)
                .collect(Collectors.toList());
    }

    @Override
    public List<Rating> getRatingsFromUser(int userId) {
        //TODO: write query to retrieve all ratings from user with id userId
        //          - Use getEmbedded for the movie doc

        return StreamSupport.stream(this.database.getCollection(USERS).aggregate(Arrays.asList(
                match(Filters.eq("_id", userId)),
                unwind("$movies"),
                project(Projections.fields(
                        computed("movieid", "$movies.movieid"),
                        computed("movierating", "$movies.rating")
                )),
                lookup(MOVIES, "movieid", "_id", "movie"),
                unwind("$movie")))
                .spliterator(), false)
                .map(document -> {
                    Document doc = ((Document) document.get("movie"));
                    return new Rating(
                            new MongoMovie(doc.getInteger("_id"),
                                    doc.getString("title"),
                                    doc.getString("genres")).convertToMovie(),
                            userId,
                            document.getInteger("movierating"));
                })
                .collect(Collectors.toList());
    }

    @Override
    public void addOrUpdateRating(Rating rating) {
        // TODO: add query which
        //         - add rating between specified user and movie if it doesn't exist
        //         - update it if it does exist

        Document matchQuery = this.database.getCollection(USERS).find(
                Filters.and(
                        Filters.eq("_id", rating.getUserId()),
                        Projections.computed("movies.movieid", rating.getMovieId())))
                .first();

        if (matchQuery == null) {
            System.out.println("Create ");
            this.database.getCollection(USERS).findOneAndUpdate(
                    Filters.eq("_id", rating.getUserId()),
                    Updates.push("movies", new MongoUserMovie(
                            rating.getMovieId(),
                            rating.getScore(),
                            new Long(System.currentTimeMillis() / 10_000).intValue())));
        } else {
            System.out.println("Update ");
            this.database.getCollection(USERS).updateOne(
                    Filters.eq("_id", rating.getUserId()),
                    Updates.combine(Updates.set("movies.$[elem].rating", rating.getScore()),
                            Updates.set("movies.$[elem].timestamp", new Long(System.currentTimeMillis() / 10_000).intValue())),
                    new UpdateOptions()
                            .arrayFilters(Collections.singletonList(Projections.computed("elem.movieid", rating.getMovieId())))
                            .upsert(true));
        }
    }

    @Override
    public List<Rating> processRecommendationsForUser(int userId, int processingMode) {
        // TODO: process recommendations for specified user exploiting other users ratings
        //       use different methods depending on processingMode parameter

        switch (processingMode) {
            case 1:
                return this.processRecommendationsForUserVariant1(userId);
            case 2:
                return this.processRecommendationsForUserVariant2(userId);
            case 3:
                return this.processRecommendationsForUserVariant3(userId);
            default:
                return new ArrayList<>();
        }
    }

    private List<Rating> processRecommendationsForUserVariant1(int userId) {

        System.out.println();
        System.out.println();
        System.out.println("First");

        List<Integer> movieids = StreamSupport.stream(this.database.getCollection(USERS).aggregate(Arrays.asList(
                match(Filters.eq("_id", userId)),
                Aggregates.unwind("$movies"),
                Aggregates.replaceRoot("$movies"),
                Projections.computed("$unset", "timestamp")
        )).spliterator(), false)
                .map(document -> document.getInteger("movieid"))
                .collect(Collectors.toList());

        System.out.println();
        System.out.println(movieids.size());
        System.out.println(movieids.stream().map(Object::toString).collect(Collectors.joining(",")));

        System.out.println();
        System.out.println();
        System.out.println("Second");

        return StreamSupport.stream(this.database.getCollection(USERS).aggregate(
                Arrays.asList(
                        match(ne("_id", userId)),
                        project(fields(
                                computed("movies",
                                        computed("$filter",
                                                fields(
                                                        computed("input", "$movies"),
                                                        computed("as", "movie"),
                                                        in("cond", Arrays.asList("$$movie.movieid", movieids))))),
                                computed("moviesA", "$movies"))),
                        computed("$set", computed("count", computed("$size", "$movies"))),
                        sort(descending("count")),
                        limit(1),
                        project(fields(
                                computed("movies",
                                        eq("$setDifference", Arrays.asList("$moviesA", "$movies"))))),
                        unwind("$movies"),
                        replaceRoot(
                                computed("$mergeObjects", Arrays.asList(
                                        computed("_id", "$_id"),
                                        "$movies"))),
                        lookup("movies", "movieid", "_id", "movie"),
                        unwind("$movie"),
                        project(fields(
                                excludeId(),
                                computed("userId", "$_id"),
                                computed("score", "$rating"),
                                computed("movie", fields(
                                        computed("id", "$movie._id"),
                                        computed("title", "$movie.title"),
                                        computed("genres",
                                                eq("$split", Arrays.asList("$movie.genres", "|")))))))
                ))
                .spliterator(), false)
                .map(document -> {
                    Document doc = ((Document) document.get("movie"));
                    return new Rating(
                            new Movie(doc.getInteger("id"),
                                    doc.getString("title"),
                                    doc.getList("genres", String.class).stream()
                                            .map(Genre::new)
                                            .collect(Collectors.toList())),
                            document.getInteger("userId"),
                            document.getInteger("score"));
                })
                .collect(Collectors.toList());
    }

    private List<Rating> processRecommendationsForUserVariant2(int userId) {
        List<Integer> movieids = StreamSupport.stream(this.database.getCollection(USERS).aggregate(Arrays.asList(
                match(Filters.eq("_id", userId)),
                Aggregates.unwind("$movies"),
                Aggregates.replaceRoot("$movies"),
                Projections.computed("$unset", "timestamp")
        )).spliterator(), false)
                .map(document -> document.getInteger("movieid"))
                .collect(Collectors.toList());

        List<Rating> ratings = StreamSupport.stream(
                this.database.getCollection(USERS).aggregate(Arrays.asList(
                        match(ne("_id", userId)),
                        project(fields(
                                computed("movies",
                                        computed("$filter", fields(
                                                computed("input", "$movies"),
                                                computed("as", "movie"),
                                                in("cond", Arrays.asList("$$movie.movieid", movieids))))),
                                computed("moviesA", "$movies"))),
                        computed("$set", computed("count", computed("$size", "$movies"))),
                        sort(descending("count")),
                        limit(5),
                        project(computed("movies", eq("$setDifference", Arrays.asList("$moviesA", "$movies")))),
                        unwind("$movies"),
                        replaceRoot(new Document("$mergeObjects", Arrays.asList(new Document("_id", "$_id"), "$movies"))),
                        group("$movieid", avg("rating", "$rating")),
                        lookup("movies", "_id", "_id", "movie"), unwind("$movie"),
                        replaceRoot(new Document("$mergeObjects", Arrays.asList(new Document("rating", "$rating"), "$movie"))),
                        eq("$set", eq("genres", eq("$split", Arrays.asList("$genres", "|")))),
                        sort(descending("rating"))))
                    .spliterator(), false)
                .map(document -> new Rating(
                        new Movie(
                                document.getInteger("_id"),
                                document.getString("title"),
                                document.getList("genres", String.class).stream().map(Genre::new).collect(Collectors.toList())),
                        0,
                        document.getDouble("rating").intValue()))
                .collect(Collectors.toList());

        return ratings;
    }

    private List<Rating> processRecommendationsForUserVariant3(int userId) {

        List<Rating> ratings = StreamSupport.stream(
                this.database.getCollection(USERS).aggregate(Arrays.asList(
                        match(ne("_id", userId)),
                        new Document("$lookup",
                                new Document("from", "users")
                                        .append("pipeline", Arrays.asList(new Document("$match",
                                                        new Document("_id", userId)),
                                                new Document("$unwind",
                                                        new Document("path", "$movies")),
                                                new Document("$replaceRoot",
                                                        new Document("newRoot", "$movies")),
                                                new Document("$unset", "timestamp")))
                                        .append("as", "userMovies")),
                        eq("$set",
                                eq("moviesA",
                                        eq("$setIntersection", Arrays.asList("$movies.movieid", "$userMovies.movieid")))),
                        new Document("$set",
                                new Document("count",
                                        new Document("$sum",
                                                new Document("$map",
                                                        new Document("input", "$moviesA")
                                                                .append("as", "movie")
                                                                .append("in",
                                                                        new Document("$subtract", Arrays.asList(4L,
                                                                                new Document("$abs",
                                                                                        new Document("$subtract", Arrays.asList(new Document("$first",
                                                                                                        new Document("$map",
                                                                                                                new Document("input",
                                                                                                                        new Document("$filter",
                                                                                                                                new Document("input", "$movies")
                                                                                                                                        .append("as", "movie1")
                                                                                                                                        .append("cond",
                                                                                                                                                new Document("$eq", Arrays.asList("$$movie1.movieid", "$$movie")))))
                                                                                                                        .append("as", "m")
                                                                                                                        .append("in",
                                                                                                                                new Document("$toInt", "$$m.rating")))),
                                                                                                new Document("$first",
                                                                                                        new Document("$map",
                                                                                                                new Document("input",
                                                                                                                        new Document("$filter",
                                                                                                                                new Document("input", "$userMovies")
                                                                                                                                        .append("as", "movie2")
                                                                                                                                        .append("cond",
                                                                                                                                                new Document("$eq", Arrays.asList("$$movie2.movieid", "$$movie")))))
                                                                                                                        .append("as", "m")
                                                                                                                        .append("in",
                                                                                                                                new Document("$toInt", "$$m.rating")))))))))))))),
                        eq("$set",
                                eq("moviesD",
                                        eq("$setDifference", Arrays.asList("$movies.movieid", "$moviesA")))),
                        project(fields(
                                computed("movies", eq("$filter", fields(
                                        eq("input", "$movies"),
                                                eq("as", "movie"),
                                                eq("cond",
                                                        eq("$in", Arrays.asList("$$movie.movieid", "$moviesD")))))
                                        ),
                                computed("count", "$count"))),
                        sort(descending("count")),
                        limit(5),
                        unwind("$movies"),
                        replaceRoot(new Document("$mergeObjects", Arrays.asList(new Document("_id", "$_id"), "$movies"))),
                        group("$movieid", avg("rating", "$rating")),
                        lookup("movies", "_id", "_id", "movie"),
                        unwind("$movie"),
                        replaceRoot(new Document("$mergeObjects", Arrays.asList(new Document("rating", "$rating"), "$movie"))),
                        eq("$set", eq("genres", eq("$split", Arrays.asList("$genres", "|")))),
                        sort(descending("rating"))))
                        .spliterator(), false)
                .map(document -> new Rating(
                        new Movie(
                                document.getInteger("_id"),
                                document.getString("title"),
                                document.getList("genres", String.class).stream().map(Genre::new).collect(Collectors.toList())),
                        0,
                        document.getDouble("rating").intValue()))
                .collect(Collectors.toList());

        return ratings;
    }
}
