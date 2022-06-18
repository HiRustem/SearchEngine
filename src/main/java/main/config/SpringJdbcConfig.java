package main.config;

import main.model.MainUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan("com.baeldung.jdbc")
public class SpringJdbcConfig {
    @Bean
    public static DataSource mysqlDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/search_engine");
        dataSource.setUsername("root");
        dataSource.setPassword("12345678");

        return dataSource;
    }

    @Autowired
    private static JdbcTemplate jdbcTemplate = new JdbcTemplate(mysqlDataSource());

    public static int getInt(String query) {
        try {
            return jdbcTemplate.queryForObject(query, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public static int getSiteId(String url) {
        try {
            return jdbcTemplate.queryForObject("SELECT id FROM site WHERE url = " + "'" + url + "'", Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public static int getPageId(String url, int siteId) {
        try {
            return jdbcTemplate.queryForObject("SELECT id FROM pages WHERE path = " + "'" + url + "'" + " AND site_id = " + siteId, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public static List<Integer> getPageId2(String url) {
        return jdbcTemplate.query("SELECT id FROM pages WHERE path = " + "'" + url + "'",
                rs -> {
                    List<Integer> list = new ArrayList<Integer>();
                    while(rs.next()){
                        list.add(rs.getInt("id"));
                    }
                    return list;
                });
    }

    public static List<Integer> getLemmaId(String lemma, int siteId) {
        return jdbcTemplate.query("SELECT id FROM lemmas WHERE lemma = " + "'" + lemma + "'" + " AND site_id = " + siteId,
                rs -> {
                    List<Integer> list = new ArrayList<Integer>();
                    while(rs.next()){
                        list.add(rs.getInt("id"));
                    }
                    return list;
                });

    }

    public static float getWeight(String type) {
        try {
            return jdbcTemplate.queryForObject("SELECT weight FROM fields WHERE name = " + "'" + type + "'", Float.class);
        } catch (EmptyResultDataAccessException e) {
            return 0.0f;
        }
    }

    public static int getIndexId(int pageId, int lemmaId) {
        try {
            return jdbcTemplate.queryForObject("SELECT id FROM `index` WHERE page_id = " + pageId + " AND lemma_id = " + lemmaId, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }


    public static String getString(String query) {
        try {
            return jdbcTemplate.queryForObject(query, String.class);
        } catch (EmptyResultDataAccessException e) {
            return "";
        }
    }

    public static List<Integer> listId(String query) {
        return jdbcTemplate.query(query,
                rs -> {
                    List<Integer> list = new ArrayList<Integer>();
                    while(rs.next()){
                        list.add(rs.getInt("id"));
                    }
                    return list;
                });
    }

    public static List<Integer> lemmaListId(String lemma) {
        return jdbcTemplate.query("SELECT id FROM lemmas WHERE lemma = " + "'" + lemma + "'",
                rs -> {
                    List<Integer> list = new ArrayList<Integer>();
                    while(rs.next()){
                        list.add(rs.getInt("id"));
                    }
                    return list;
                });
    }

    public static List<Integer> lemmaListId2(String lemma, int siteId) {
        return jdbcTemplate.query("SELECT id FROM lemmas WHERE lemma = " + "'" + lemma + "' AND site_id = " + siteId,
                rs -> {
                    List<Integer> list = new ArrayList<Integer>();
                    while(rs.next()){
                        list.add(rs.getInt("id"));
                    }
                    return list;
                });
    }

    public static List<Float> rankList(String query) {
        return jdbcTemplate.query(query,
                rs -> {
                    List<Float> list = new ArrayList<>();
                    while(rs.next()){
                        list.add(rs.getFloat("rank"));
                    }
                    return list;
                });
    }

    public static List<String> getLemmas(String normal, int siteId) {
        return jdbcTemplate.query("SELECT lemma FROM lemmas WHERE lemma = " + "'" + normal + "'" + " AND site_id = " + siteId,
                rs -> {
                    List<String> list = new ArrayList<>();
                    while(rs.next()){
                        list.add(rs.getString("lemma"));
                    }
                    return list;
                });
    }

    public static List<String> getStatus(String query) {
        return jdbcTemplate.query(query,
                rs -> {
                    List<String> list = new ArrayList<>();
                    while(rs.next()){
                        list.add(rs.getString("status"));
                    }
                    return list;
                });
    }

    public static List<Integer> pageId(String query) {
        return jdbcTemplate.query(query,
                rs -> {
                    List<Integer> list = new ArrayList<Integer>();
                    while(rs.next()){
                        list.add(rs.getInt("page_id"));
                    }
                    return list;
                });
    }

    public static List<Integer> frequencyList(String lemma) {
        return jdbcTemplate.query("SELECT frequency FROM lemmas WHERE lemma = " + "'" + lemma + "'",
                rs -> {
                    List<Integer> list = new ArrayList<Integer>();
                    while(rs.next()){
                        list.add(rs.getInt("frequency"));
                    }
                    return list;
                });
    }

    public static List<Integer> lemmaId(String query) {
        return jdbcTemplate.query(query,
                rs -> {
                    List<Integer> list = new ArrayList<Integer>();
                    while(rs.next()){
                        list.add(rs.getInt("lemma_id"));
                    }
                    return list;
                });
    }

    public static void update(String query) {
        jdbcTemplate.update(query);
    }

    public static void updateLemma(String normal, int siteId) {
        jdbcTemplate.update("UPDATE lemmas SET frequency = frequency + 1 WHERE lemma = " + "'" + normal + "'" + " AND site_id = " + siteId);
    }

    public static void insertLemma(String normal, int siteId) {
        jdbcTemplate.update("INSERT INTO lemmas (lemma, frequency, site_id) VALUES (" + "'" + normal + "'" + ", " + "1, " + siteId + ")");
    }

    public static void updateSiteTime(String date, MainUrl mainUrl) {
        jdbcTemplate.update("UPDATE site SET status_time = " + "'" + date + "'" + " WHERE url = " + "'" + mainUrl + "'");
    }

    public static void updateIndex(float rank, int indexId) {
        jdbcTemplate.update("UPDATE `index` SET `rank` = `rank` + " + rank + " WHERE id = " + indexId);
    }

    public static void insertIndex(int pageId, int lemmaId, float rank) {
        jdbcTemplate.update("INSERT INTO `index` (page_id, lemma_id, `rank`) VALUES (" + pageId + ", " + lemmaId + ", " + rank + ")");
    }

    public static void insertSite(String status, String date, String url, String title) {
        jdbcTemplate.update("INSERT INTO site (status, status_time, url, name) VALUES (" + "'" + status + "'" + ", " + "'" + date + "'" + ", " + "'" + url + "'" + ", " + "'" + title + "'" + ")");
    }

    public static void updateSite(String status, String url) {
        jdbcTemplate.update("UPDATE site SET status = " + "'" + status + "'" + "WHERE url = " + "'" + url + "'");
    }

    public static void updateSite2(String status, String date, String error, String url) {
        jdbcTemplate.update("UPDATE site SET status = " + "'" + status + "'" + ", status_time = " + "'" + date + "'" + ", last_error = " + "'" + error + "' WHERE url = " + "'" + url + "'");
    }

    public static void updateSiteError(String status, String error, String date, String url) {
        jdbcTemplate.update("UPDATE site SET status = " + "'" + status + "'" + ", last_error = " + "'" + error + "'" + ", " + "'" + date + "'" + " WHERE url = " + "'" + url + "'");
    }

    public static void insertPage(String path, int code, String content, int site_id) {
        jdbcTemplate.update("INSERT INTO pages (path, code, content, site_id) VALUES (?, ?, ?, ?)", path, code, content, site_id);
    }

    public static void deleteQuery(String query) {
        jdbcTemplate.update(query);
    }

    public static Integer count(String query) {return jdbcTemplate.queryForObject(query, Integer.class);}

}
