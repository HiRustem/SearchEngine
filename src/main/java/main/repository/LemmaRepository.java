package main.repository;

import main.model.MainUrl;
import main.config.SpringJdbcConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class LemmaRepository {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");

    public static void addInLemmas(HashSet<String> hashSet, MainUrl mainUrl) {
        int siteId = SpringJdbcConfig.getSiteId(mainUrl.getMainUrl());
        if (siteId != 0) {
            hashSet.forEach(normal -> {
                List<String> lemmas = SpringJdbcConfig.getLemmas(normal, siteId);
                if (!lemmas.isEmpty()) {
                    SpringJdbcConfig.updateLemma(normal, siteId);
                } else {
                    SpringJdbcConfig.insertLemma(normal, siteId);
                }
            });
        }
    }

    public static void addInIndex(HashMap<String, Integer> hashMap, String url, String type, MainUrl mainUrl) {
        int siteId = SpringJdbcConfig.getSiteId(mainUrl.getMainUrl());
        if (siteId != 0) {
            hashMap.forEach((k, v) -> {
                int pageId = SpringJdbcConfig.getPageId(url, siteId);
                List<Integer> lemmaId = SpringJdbcConfig.getLemmaId(k, siteId);
                float fieldWeight = SpringJdbcConfig.getWeight(type);

                if (pageId != 0 && !lemmaId.isEmpty() && fieldWeight != 0) {
                    int indexId = SpringJdbcConfig.getIndexId(pageId, lemmaId.get(0));
                    float rank = v * fieldWeight;
                    if (indexId != 0) {
                        SpringJdbcConfig.updateIndex(rank, indexId);
                    } else {
                        SpringJdbcConfig.insertIndex(pageId, lemmaId.get(0), rank);
                    }
                }
                Date dateNow = new Date();
                SpringJdbcConfig.updateSiteTime(FORMAT.format(dateNow), mainUrl);
            });
        }
    }
}
