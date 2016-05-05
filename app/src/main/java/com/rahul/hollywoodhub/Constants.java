package com.rahul.hollywoodhub;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 * Created by Rahul on 17 Apr 2016.
 */
public class Constants {
    public static final String MOVIE_BASE_URL = "http://123movies.ru/movie/filter/movie/";
    public static final String SERIES_BASE_URL = "http://123movies.ru/movie/filter/series/";
    public static final String MOVIE_SEARCH_PREFIX = "http://123movies.ru/movie/search/";
    public static final String LOAD_EPISODE_PREFIX_1 = "http://123movies.ru/ajax/movie_load_episode/";
    public static final String GET_EPISODES_PREFIX_1 = "http://123movies.ru/ajax/movie_get_episodes/";
    public static final String LOAD_EPISODE_PREFIX = "http://123movies.ru/ajax/load_episode/";
    public static final String GET_EPISODES_PREFIX = "http://123movies.ru/ajax/get_episodes/";
    public static final String SHORTEST_API_TOKEN_LINK = "http://sh.st/st/4aac64566051e85abb20f14559036ed9/";
    public static final String DEFAULT_URL = "http://123movies.ru";
    public static final String UPDATE_CHECKER_URL = "http://rahulpandey1501-appupdates.blogspot.in/";
    public static final LinkedHashMap<String, String> GENRE_MAPPING, SECTION_MAPPING;
    static
    {
        SECTION_MAPPING = new LinkedHashMap<>();
        GENRE_MAPPING = new LinkedHashMap<>();
        GENRE_MAPPING.put("All", "all");
        GENRE_MAPPING.put("Action", "1");
        GENRE_MAPPING.put("Adventure", "2");
        GENRE_MAPPING.put("Animation", "120");
        GENRE_MAPPING.put("Biography", "125");
        GENRE_MAPPING.put("Comedy", "7");
        GENRE_MAPPING.put("Costume", "12");
        GENRE_MAPPING.put("Crime", "25");
        GENRE_MAPPING.put("Documentary", "126");
        GENRE_MAPPING.put("Drama", "119");
        GENRE_MAPPING.put("Family", "114");
        GENRE_MAPPING.put("Fantasy", "124");
        GENRE_MAPPING.put("History", "112");
        GENRE_MAPPING.put("Horror", "122");
        GENRE_MAPPING.put("Kungfu", "6");
        GENRE_MAPPING.put("Musical", "27");
        GENRE_MAPPING.put("Mystery", "121");
        GENRE_MAPPING.put("Mythological", "11");
        GENRE_MAPPING.put("Psychological", "9");
        GENRE_MAPPING.put("Romance", "4");
        GENRE_MAPPING.put("Sci-Fi", "10");
        GENRE_MAPPING.put("Sitcom", "118");
        GENRE_MAPPING.put("Sport", "123");
        GENRE_MAPPING.put("Thriller", "3");
        GENRE_MAPPING.put("TV Show", "23");
        GENRE_MAPPING.put("War", "22");

        SECTION_MAPPING.put("Popular", "favorite");
        SECTION_MAPPING.put("Most Viewed", "view");
        SECTION_MAPPING.put("Top IMDB", "imdb_mark");
        SECTION_MAPPING.put("Top Rated", "rating");
        SECTION_MAPPING.put("Latest", "latest");
    }
}
