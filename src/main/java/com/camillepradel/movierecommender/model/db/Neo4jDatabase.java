package com.camillepradel.movierecommender.model.db;

import com.camillepradel.movierecommender.model.Genre;
import com.camillepradel.movierecommender.model.Movie;
import com.camillepradel.movierecommender.model.Rating;
import org.neo4j.driver.*;

import java.util.*;

import static org.neo4j.driver.Values.parameters;

public class Neo4jDatabase extends AbstractDatabase {


    private Driver driver;
    private static final String USER = "admin";
    private static final String PASSWORD = "password";
    private static final String URI = "bolt://localhost:7687";

    public Neo4jDatabase() {
        this.driver = GraphDatabase.driver( URI, AuthTokens.basic( USER, PASSWORD ) );
    }

    @Override
    public List<Movie> getAllMovies() {
        // TODO: write query to retrieve all movies from DB

        String matchQuery = "MATCH (m:Movie)-[:CATEGORIZED_AS]->(g:Genre) RETURN m, collect(g) as genres";
        try ( Session session = driver.session() )
        {
            return session.run(matchQuery).list(r ->
                    new Movie(r.get(0).get("id").asInt(),
                            r.get(0).get("title").asString(),
                            r.get(1).asList(value ->
                                    new Genre(value.get("id").asInt(),
                                            value.get("name").asString()))));
        }
    }

    @Override
    public List<Movie> getMoviesRatedByUser(int userId) {
        // TODO: write query to retrieve all movies rated by user with id userId

        String matchQuery = "MATCH (u:User {id: 1})-[:RATED]->(m:Movie)-[:CATEGORIZED_AS]->(g:Genre) RETURN m, collect(g) as genres";

        try (Session session = driver.session()) {
            return session.run(matchQuery, parameters("id", userId)).list(r ->
                    new Movie(r.get(0).get("id").asInt(),
                            r.get(0).get("title").asString(),
                            r.get(1).asList(value ->
                                    new Genre(value.get("id").asInt(),
                                            value.get("name").asString()))));
        }
    }

    @Override
    public List<Rating> getRatingsFromUser(int userId) {
        // TODO: write query to retrieve all ratings from user with id userId

        String matchQuery = "MATCH (u:User {id: $id})-[r:RATED]->(m:Movie)-[:CATEGORIZED_AS]->(g:Genre) " +
                            "RETURN r, m, collect(g) as genres";
        try (Session session = driver.session()) {
            return session.run(matchQuery, parameters("id", userId)).list(r ->
                    new Rating(new Movie(r.get(1).get("id").asInt(),
                            r.get(1).get("title").asString(),
                            r.get(2).asList(value ->
                                    new Genre(value.get("id").asInt(),
                                            value.get("name").asString()))),
                            userId,
                            r.get(0).get("note").asInt()));
        }
    }

    @Override
    public void addOrUpdateRating(Rating rating) {

        String matchQuery = "MATCH (u:User {id: $id})-[r:RATED]->(m:Movie {id: $idmovie}) RETURN r";

        Value parameters = parameters("id", rating.getUserId(), "idmovie", rating.getMovieId(), "note", rating.getScore());

        try (Session session = driver.session()) {
            if (session.run(matchQuery, parameters).list().isEmpty()) {
                session.run("MATCH (u:User {id: $id}), (m:Movie {id: $idmovie}) " +
                                "CREATE (u)-[r:RATED {note: $note}]->(m) " +
                                "RETURN r",
                        parameters);
            } else {
                session.run("MATCH (u:User {id: $id})-[r:RATED]->(m:Movie {id: $idmovie})" +
                                "SET r.note = $note " +
                                "RETURN r",
                        parameters);
            }
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

        String matchQuery = "MATCH (target_user:User {id : 1})-[:RATED]->(m:Movie)<-[:RATED]-(other_user:User)\n" +
                "WITH other_user, count(distinct m.title) AS num_common_movies, target_user\n" +
                "ORDER BY num_common_movies \n" +
                "DESC\n" +
                "LIMIT 1\n" +
                "MATCH (other_user)-[rat_other_user:RATED]->(m2:Movie)-[:CATEGORIZED_AS]->(g:Genre)\n" +
                "WHERE NOT ((target_user)-[:RATED]->(m2))\n" +
                "RETURN m2 AS movie, collect(g) AS genres, rat_other_user.note AS rating\n" +
                "ORDER BY rat_other_user.note \n" +
                "DESC";
        try (Session session = driver.session()) {
            return session.run(matchQuery, parameters("id", userId)).list(r ->
                    new Rating(new Movie(r.get("movie").get("id").asInt(),
                            r.get("movie").get("title").asString(),
                            r.get("genres").asList(value ->
                                    new Genre(value.get("id").asInt(),
                                            value.get("name").asString()))),
                            userId,
                            r.get("rating").asInt()));
        }
    }

    private List<Rating> processRecommendationsForUserVariant2(int userId) {

        String matchQuery = "MATCH (target_user:User {id : 1})-[:RATED]->(m:Movie)<-[:RATED]-(other_user:User)\n" +
                "WITH other_user, count(distinct m.title) AS num_common_movies, target_user\n" +
                "ORDER BY num_common_movies \n" +
                "DESC\n" +
                "LIMIT 5\n" +
                "MATCH (other_user)-[rat_other_user:RATED]->(m2:Movie)-[:CATEGORIZED_AS]->(g:Genre)\n" +
                "WHERE NOT ((target_user)-[:RATED]->(m2))\n" +
                "WITH DISTINCT m2, g, avg(rat_other_user.note) AS average, count(m2) AS count_rat\n" +
                "ORDER BY average DESC, count_rat DESC\n" +
                "RETURN m2 AS movie, collect(g) AS genres, round(100 * average) / 100 AS rating";

        try (Session session = driver.session()) {
            return session.run(matchQuery, parameters("id", userId)).list(r ->
                    new Rating(new Movie(r.get("movie").get("id").asInt(),
                            r.get("movie").get("title").asString(),
                            r.get("genres").asList(value ->
                                    new Genre(value.get("id").asInt(),
                                            value.get("name").asString()))),
                            userId,
                            (int) r.get("rating").asDouble()));
        }
    }

    private List<Rating> processRecommendationsForUserVariant3(int userId) {
        String matchQuery = "MATCH (target_user:User {id : 1})-[t_rat:RATED]->(m:Movie)<-[o_rat:RATED]-(other_user:User)\n" +
                "WITH other_user, SUM(4 - abs(t_rat.note - o_rat.note)) AS sim, target_user\n" +
                "ORDER BY sim\n" +
                "DESC\n" +
                "LIMIT 5\n" +
                "MATCH (other_user)-[rat_other_user:RATED]->(m2:Movie)-[:CATEGORIZED_AS]->(g:Genre)\n" +
                "WHERE NOT ((target_user)-[:RATED]->(m2))\n" +
                "WITH DISTINCT m2, g, avg(rat_other_user.note) AS average, count(m2) AS movie_count\n" +
                "ORDER BY average DESC, movie_count DESC\n" +
                "RETURN m2 AS movie, collect(g) AS genres, round(average) AS rating";

        try (Session session = driver.session()) {
            return session.run(matchQuery, parameters("id", userId)).list(r ->
                    new Rating(new Movie(r.get("movie").get("id").asInt(),
                            r.get("movie").get("title").asString(),
                            r.get("genres").asList(value ->
                                    new Genre(value.get("id").asInt(),
                                            value.get("name").asString()))),
                            userId,
                            (int) r.get("rating").asDouble()));
        }
    }
}
