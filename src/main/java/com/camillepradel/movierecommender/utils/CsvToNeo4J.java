package com.camillepradel.movierecommender.utils;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import static org.neo4j.driver.Values.parameters;

public class CsvToNeo4J {

    static final String pathToCsvFiles = "/home/valo/IdeaProjects/MovieRecommender/src/main/java/com/camillepradel/movierecommender/utils/";
    static final String usersCsvFile = pathToCsvFiles + "users.csv";
    static final String moviesCsvFile = pathToCsvFiles + "movies.csv";
    static final String genresCsvFile = pathToCsvFiles + "genres.csv";
    static final String movGenreCsvFile = pathToCsvFiles + "mov_genre.csv";
    static final String ratingsCsvFile = pathToCsvFiles + "ratings.csv";
    static final String friendsCsvFile = pathToCsvFiles + "friends.csv";
    static final String cvsSplitBy = ",";

    private static void commitUsers(Driver driver) {
        System.out.println(usersCsvFile);

        String insertQuery = ":auto USING PERIODIC COMMIT\n" +
                "LOAD CSV WITH HEADERS FROM $url AS users\n" +
                "CREATE (u:User {id : toInteger(users.id), sex : users.sex, age : toInteger(users.age),occupation : users.occupation, zip_code : users.zip_code});";

        driver.session().run(insertQuery, parameters("url", usersCsvFile));
    }

    private static void commitMovies(Driver driver) {
        System.out.println(moviesCsvFile);

        String insertQuery = ":auto USING PERIODIC COMMIT\n" +
                "LOAD CSV WITH HEADERS FROM 'http://camillepradel.fr/teaching/nosql/movielens_export/movies.csv' AS movies\n" +
                "CREATE (m:Movie {id : toInteger(movies.id), title : movies.title, date : toInteger(movies.date)});";

        driver.session().run(insertQuery, parameters("url", moviesCsvFile));
    }

    private static void commitGenres(Driver driver) {

        System.out.println(genresCsvFile);

        String insertQuery = ":auto USING PERIODIC COMMIT\n" +
                "LOAD CSV WITH HEADERS FROM 'http://camillepradel.fr/teaching/nosql/movielens_export/genres.csv' AS genres\n" +
                "CREATE (u:Genre {id : toInteger(genres.id), name : genres.name});";

        driver.session().run(insertQuery, parameters("url", genresCsvFile));
    }

    private static void commitMovieGenre(Driver driver) {

        System.out.println(movGenreCsvFile);

        String insertQuery = ":auto USING PERIODIC COMMIT\n" +
                "LOAD CSV WITH HEADERS FROM 'http://camillepradel.fr/teaching/nosql/movielens_export/mov_genre.csv' AS mov_genre\n" +
                "MERGE (m : Movie { id : toInteger(mov_genre.mov_id) })\n" +
                "MERGE (g : Genre { id : toInteger(mov_genre.genre) })\n" +
                "CREATE (m)-[:CATEGORIZED_AS]->(g);";

        driver.session().run(insertQuery, parameters("url", movGenreCsvFile));
    }

    private static void commitRatings(Driver driver) {

        System.out.println(ratingsCsvFile);

        String insertQuery = ":auto USING PERIODIC COMMIT\n" +
                "LOAD CSV WITH HEADERS FROM 'http://camillepradel.fr/teaching/nosql/movielens_export/ratings.csv' AS ratings\n" +
                "MERGE (u : User { id : toInteger(ratings.user_id) })\n" +
                "MERGE (m : Movie { id : toInteger(ratings.mov_id) })\n" +
                "CREATE (u)-[:RATED { note : toInteger(ratings.rating), timestamp : toInteger(ratings.timestamp) } ]->(m);";

        driver.session().run(insertQuery, parameters("url", ratingsCsvFile));
    }

    private static void commitFriends(Driver driver) {

        System.out.println(friendsCsvFile);

        String insertQuery = ":auto USING PERIODIC COMMIT\n" +
                "LOAD CSV WITH HEADERS FROM 'http://camillepradel.fr/teaching/nosql/movielens_export/friends.csv' AS friends\n" +
                "MERGE (u1:User { id : toInteger(friends.user1_id) })\n" +
                "MERGE (u2:User { id : toInteger(friends.user2_id) })\n" +
                "CREATE (u1)-[:FRIEND_OF]->(u2);";

        driver.session().run(insertQuery, parameters("url", friendsCsvFile));
    }


    public static void main(String[] args) {
        // load JDBC driver

        String USER = "admin";
        String PASSWORD = "password";
        String URI = "bolt://localhost:7687";

        Driver driver = GraphDatabase.driver( URI, AuthTokens.basic( USER, PASSWORD ) );

        driver.session().run("CREATE OR REPLACE DATABASE neo4j");

        commitUsers(driver);
        commitMovies(driver);
        commitGenres(driver);
        commitMovieGenre(driver);
        commitRatings(driver);
        commitFriends(driver);

        System.out.println("done");
    }
}
