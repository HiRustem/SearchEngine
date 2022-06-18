package main.repository;
import main.controller.SearchController;
import main.model.MainUrl;
import main.service.LemmaService;
import main.config.SpringJdbcConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class LinkExecutorRepository extends RecursiveAction {
    private String url;
    private MainUrl mainUrl;
    private Date dateNow;

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");

    private static final String CSS_QUERY = "a[href]";
    private static final String ATTRIBUTE_KEY = "href";
    private static final String ERROR_STATUS = "FAILED";
    private static final String REFERRER = "http://www.google.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    private static final String URL_REPLACEMENT = "/";
    private static final String ATTRIBUTE_GRID = "#";

    public LinkExecutorRepository(String url, MainUrl mainUrl) {
        this.url = url;
        this.mainUrl = mainUrl;
    }

    @Override
    protected void compute() {
        Document document;
        Elements elements;
        try {
            Thread.sleep(1000);
            document = Jsoup.connect(url).ignoreContentType(true).userAgent(USER_AGENT).referrer(REFERRER).get();

            elements = document.select(CSS_QUERY);
            for (Element element : elements) {
                if (!SearchController.isContinue) break;
                String attributeUrl = element.absUrl(ATTRIBUTE_KEY);
                List<Integer> pageId = SpringJdbcConfig.getPageId2(attributeUrl.replaceAll(url, URL_REPLACEMENT));

                if (pageId.isEmpty() && !attributeUrl.isEmpty() && attributeUrl.startsWith(url) && !attributeUrl.contains(ATTRIBUTE_GRID)) {
                    LinkExecutorRepository linkExecutor = new LinkExecutorRepository(attributeUrl, mainUrl);
                    linkExecutor.fork();
                    int status = document.connection().response().statusCode();
                    int siteId = SpringJdbcConfig.getSiteId(mainUrl.getMainUrl());
                    if (siteId != 0) {
                        SpringJdbcConfig.insertPage(attributeUrl.replaceAll(url, URL_REPLACEMENT), status, document.toString(), siteId);
                        if (status == 200) {
                            LemmaService.getLemma(document.title(), document.body().text(), attributeUrl.replaceAll(url, URL_REPLACEMENT), mainUrl);
                        } else {
                            dateNow = new Date();
                            SpringJdbcConfig.updateSiteError(ERROR_STATUS, document.toString(), FORMAT.format(dateNow), mainUrl.getMainUrl());
                        }
                    }
                }
            }
        } catch (InterruptedException | IOException | SQLException e) {
            dateNow = new Date();
            SpringJdbcConfig.updateSite2(ERROR_STATUS, FORMAT.format(dateNow), e.toString(), mainUrl.getMainUrl());
        }
    }
}
