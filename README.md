# MovieRecommender

## Requierements

* Docker
* Maven
* mongo (CLI) (for shell connection)

## Launcher

#### Application

1) Install packages
    ```shell
    mvn clean install
    ```

2) Start the application
    ```shell
     mvn tomcat7:run -X
    ```

#### Docker services
```shell
docker-compose up -d
```

#### Import data

Mongo importation
```shell
mvn exec:java@mongo-import
```

SQL importation
```shell
mvn exec:java@sql-import
```

Neo4J importation
```shell
mvn exec:java@neo4j-import
```


#### Mongo Shell via terminal
```shell
mongo -u root -p root --authenticationDatabase admin MovieLens
```

## Links

* http://localhost:8080/MovieRecommender/
* http://localhost:8080/MovieRecommender/hello?name=UT2
* http://localhost:8080/MovieRecommender/movies
* http://localhost:8080/MovieRecommender/movies?user_id=1
* http://localhost:8080/MovieRecommender/movieratings?user_id=1
* http://localhost:8080/MovieRecommender/recommendations?user_id=1&processing_mode=1
* http://localhost:8080/MovieRecommender/recommendations?user_id=1&processing_mode=2
* http://localhost:8080/MovieRecommender/recommendations?user_id=1&processing_mode=3

## Benchmark

* TestRe

## Mongo requests

### Recommendation (3)

```json5
[{
    $match: {
        _id: {$ne: 2}
    }
}, {
    $lookup: {
        from: 'users',
        pipeline: [
            {
                $match: {
                    _id: 2
                }
            },
            {
                $unwind: {
                    path: "$movies"
                }
            },
            {
                $replaceRoot: {
                    newRoot: "$movies"
                }
            },
            {
                $unset: "timestamp"
            }],
        as: 'userMovies'
    }
}, {
    $set: {
        moviesA: {
            $setIntersection: ["$movies.movieid", "$userMovies.movieid"]
        }
    }
}, {
    $set: {
        count: {
            $sum: {
                $map: {
                    input: "$moviesA",
                    as: "movie",
                    in: {
                        $subtract: [
                            4,
                            {
                                $abs: {
                                    $subtract: [
                                        {
                                            $first: {
                                                $map: {
                                                    input: {
                                                        $filter: {
                                                            input: "$movies",
                                                            as: "movie1",
                                                            cond: {$eq: ["$$movie1.movieid", "$$movie"]}
                                                        }
                                                    },
                                                    as: "m",
                                                    in: {$toInt: "$$m.rating"}
                                                }
                                            }
                                        },
                                        {
                                            $first: {
                                                $map: {
                                                    input: {
                                                        $filter: {
                                                            input: "$userMovies",
                                                            as: "movie2",
                                                            cond: {$eq: ["$$movie2.movieid", "$$movie"]}
                                                        }
                                                    },
                                                    as: "m",
                                                    in: {$toInt: "$$m.rating"}
                                                }
                                            }
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            }
        }
    }
}, {
    $set: {
        moviesD: {
            $setDifference: ["$movies.movieid", "$moviesA"]
        }
    }
}, {
    $project: {
        movies: {
            $filter: {
                input: "$movies",
                as: "movie",
                cond: {$in: ["$$movie.movieid", "$moviesD"]}
            }
        },
        count: "$count"
    }
},
{
    $sort: {
        count: -1,
    }
}, {$limit: 5},
{
    $unwind: {
        path: "$movies"
    }
}, {
    $replaceRoot: {
        newRoot: {$mergeObjects: [{_id: "$_id"}, "$movies"]}
    }
}, {
    $group: {
        _id: "$movieid",
        rating: {$avg: "$rating"}
    }
}, {
    $lookup: {
        from: 'movies',
        localField: '_id',
        foreignField: '_id',
        as: 'movie'
    }
}, {
    $unwind: {
        path: "$movie"
    }
}, {
    $replaceRoot: {
        newRoot: {$mergeObjects: [{rating: "$rating"}, "$movie"]}
    }
}, {
    $set: {
        genres: {$split: ["$genres", "|"]}
    }
}, {
    $sort: {
        rating: -1
    }
}]
```
