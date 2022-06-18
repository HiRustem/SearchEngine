package main.repository;

import main.controller.SearchController;
import main.dto.StatisticsDTO;
import main.dto.StatsResultDTO;
import main.dto.TotalDTO;
import main.model.PageInfo;
import main.config.SpringJdbcConfig;
import java.util.ArrayList;
import java.util.List;

public class StatisticRepository {
    private static final String SITES_COUNT = "SELECT COUNT(*) FROM site";
    private static final String PAGES_COUNT = "SELECT COUNT(*) FROM pages";
    private static final String LEMMAS_COUNT = "SELECT COUNT(*) FROM lemmas";

    private static final String SITES_QUERY = "SELECT id FROM site";

    private static final String URL_QUERY = "SELECT url FROM site WHERE id = ";
    private static final String NAME_QUERY = "SELECT name FROM site WHERE id = ";
    private static final String STATUS_QUERY = "SELECT status FROM site WHERE id = ";
    private static final String STATUS_TIME_QUERY = "SELECT status_time FROM site WHERE id = ";
    private static final String ERROR_QUERY = "SELECT last_error FROM site WHERE id = ";
    private static final String PAGES_QUERY = "SELECT COUNT(*) FROM pages WHERE site_id = ";
    private static final String LEMMAS_QUERY = "SELECT COUNT(*) FROM lemmas WHERE site_id = ";

    public static StatsResultDTO getStatistics() {
        int sites = SpringJdbcConfig.count(SITES_COUNT);
        int pages = SpringJdbcConfig.count(PAGES_COUNT);
        int lemmas = SpringJdbcConfig.count(LEMMAS_COUNT);

        List<Integer> sitesList = SpringJdbcConfig.listId(SITES_QUERY);

        TotalDTO totalDTO = new TotalDTO(sites, pages, lemmas, SearchController.isIndexing);
        List<PageInfo> pagesInfo = new ArrayList<>();

        for (Integer id : sitesList) {
            String url = SpringJdbcConfig.getString(URL_QUERY + id);
            String name = SpringJdbcConfig.getString(NAME_QUERY + id);
            String status = SpringJdbcConfig.getString(STATUS_QUERY + id);
            String time = SpringJdbcConfig.getString(STATUS_TIME_QUERY + id);
            String error = SpringJdbcConfig.getString(ERROR_QUERY + id);
            int pageCount = SpringJdbcConfig.count(PAGES_QUERY + id);
            int lemmaCount = SpringJdbcConfig.count(LEMMAS_QUERY + id);

            pagesInfo.add(new PageInfo(url, name, status, time, error, pageCount, lemmaCount));
        }
        return new StatsResultDTO(true, new StatisticsDTO(totalDTO, pagesInfo));
    }
}
