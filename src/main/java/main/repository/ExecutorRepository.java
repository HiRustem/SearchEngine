package main.repository;

import main.controller.SearchController;
import main.model.MainUrl;
import main.service.LinkExecutorService;
import main.config.SpringJdbcConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExecutorRepository {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");

    private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    private static final String REFERRER = "http://www.google.com";

    private static final String PAGES_QUERY = "SELECT id FROM pages WHERE site_id = ";
    private static final String INDEX_QUERY = "DELETE FROM `index` WHERE page_id = ";
    private static final String DELETE_PAGES = "DELETE FROM pages WHERE site_id = ";
    private static final String DELETE_LEMMAS = "DELETE FROM lemmas WHERE site_id = ";
    private static final String DELETE_SITES = "DELETE FROM site WHERE id = ";
    private static final String STATUS_QUERY = "SELECT status FROM site";

    private static final String START_STATUS = "INDEXING";
    private static final String END_STATUS = "INDEXED";


    public static void start(String url) throws SQLException, IOException {
        MainUrl mainUrl = new MainUrl(url);

        Document document = Jsoup.connect(url).userAgent(USER_AGENT).referrer(REFERRER).get();

        int siteId = SpringJdbcConfig.getSiteId(url);
        if (siteId != 0) {
            List<Integer> listId = SpringJdbcConfig.listId(PAGES_QUERY + siteId);
            for (Integer id : listId) {
                SpringJdbcConfig.deleteQuery(INDEX_QUERY + id);
            }
            SpringJdbcConfig.deleteQuery(DELETE_PAGES + siteId);
            SpringJdbcConfig.deleteQuery(DELETE_LEMMAS + siteId);
            SpringJdbcConfig.deleteQuery(DELETE_SITES + siteId);
        }
        Date dateNow = new Date();
        SpringJdbcConfig.insertSite(START_STATUS, FORMAT.format(dateNow), url, document.title());

        LinkExecutorService.executeLink(url, mainUrl);

        SpringJdbcConfig.updateSite(END_STATUS, url);

        List<String> statusList = SpringJdbcConfig.getStatus(STATUS_QUERY);
        if (!statusList.contains(START_STATUS)) SearchController.isIndexing = false;
    }
}
