package main.repository;

import main.dto.ResultDTO;
import main.model.Result;
import main.config.SpringJdbcConfig;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.apache.lucene.morphology.LuceneMorphology;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SearchRepository {
    private static final String REGEX = ".*ПРЕДЛ|.*СОЮЗ.*|.*МС-П.*|.*МЕЖД|.*ЧАСТ|.*МС.*";
    private static final String PAGE_NOT_FOUND = "Указанная страница не найдена";
    private static final String PAGE_ID_QUERY = "SELECT page_id FROM `index` WHERE lemma_id IN (";
    private static final String PAGE_ID_QUERY2 = " AND page_id IN (";
    private static final String RANK_QUERY = "SELECT `rank` FROM `index` WHERE page_id = ";
    private static final String RANK_QUERY2 = " AND lemma_id IN (";
    private static final String PATH_QUERY = "SELECT path FROM pages WHERE id = ";
    private static final String CONTENT_QUERY = "SELECT content FROM pages WHERE id = ";
    private static final String SITE_ID_QUERY = "SELECT site_id FROM pages WHERE id = ";
    private static final String URL_QUERY = "SELECT url FROM site WHERE id = ";
    private static final String NAME_QUERY = "SELECT name FROM site WHERE id = ";
    private static final String COMMA = ", ";
    private static final String BRACKET = ")";
    private static final String OPEN_TAG = "<b>";
    private static final String CLOSE_TAG = "</b>";
    private static final String WORDS_REPLACE = "[^А-Яа-я\\s]";
    private static final String WORDS_REGEX = "[А-Яа-я]{2,}";
    private static final String SPACE = " ";
    private static final String SENTENCES_REPLACE = "\\s+";
    private static final String SENTENCES_REGEX = "(?=[А-Я])";

    public static ResultDTO results(String query, String site, int offset, int limit) throws IOException {
        HashMap<String, Integer> lemmas = new HashMap<>();
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        String[] words = query
                .replaceAll(WORDS_REPLACE, SPACE)
                .toLowerCase(Locale.ROOT)
                .split(SPACE);
        for (String word : words) {
            if (word.matches(WORDS_REGEX)) {
                String normal = luceneMorph.getNormalForms(word).get(0);
                String morph = luceneMorph.getMorphInfo(normal).get(0);
                if (!morph.matches(REGEX)) {
                    List<Integer> frequencyList = SpringJdbcConfig.frequencyList(normal);
                    if (!frequencyList.isEmpty()) {
                        for (Integer frequency : frequencyList) {
                            lemmas.put(normal, frequency);
                        }
                    } else {
                        return new ResultDTO(false, PAGE_NOT_FOUND);
                    }
                }
            }
        }
        return relevance(lemmas, sort(lemmas, site), site, offset, limit);
    }

    public static List<Integer> sort(HashMap<String, Integer> hashMap, String site) {
        ArrayList<Integer> sortedPages = new ArrayList<>();
        List<Integer> lemmaList;

        HashMap<String, Integer> sortedMap = hashMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,  e2) -> e1, LinkedHashMap::new));
        for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
            int siteId = SpringJdbcConfig.getSiteId(site);
            if (siteId != 0) {
                lemmaList = SpringJdbcConfig.lemmaListId2(entry.getKey(), siteId);
            } else {
                lemmaList = SpringJdbcConfig.lemmaListId(entry.getKey());
            }
            if (!lemmaList.isEmpty()) {
                StringBuilder query = new StringBuilder();
                if(sortedPages.isEmpty()) {
                    query.append(PAGE_ID_QUERY);
                    for (int i = 0; i < lemmaList.size(); i++) {
                        if (i == 0) {
                            query.append(lemmaList.get(i));
                        } else {
                            query.append(COMMA).append(lemmaList.get(i));
                        }
                    }
                    query.append(BRACKET);
                    List<Integer> page_id = SpringJdbcConfig.pageId(query.toString());
                    sortedPages.addAll(page_id);
                } else {
                    query.append(PAGE_ID_QUERY);
                    for (int i = 0; i < lemmaList.size(); i++) {
                        if (i == 0) {
                            query.append(lemmaList.get(i));
                        } else {
                            query.append(COMMA).append(lemmaList.get(i));
                        }
                    }
                    query.append(BRACKET).append(PAGE_ID_QUERY2);
                    for (int i = 0; i < sortedPages.size(); i++) {
                        if (i == 0) {
                            query.append(sortedPages.get(i));
                        } else {
                            query.append(COMMA).append(sortedPages.get(i));
                        }
                    }
                    query.append(BRACKET);

                    List<Integer> listId = SpringJdbcConfig.listId(query.toString());
                    ArrayList<Integer> newList = new ArrayList<>();

                    for (Integer id : listId) {
                        if (listId.contains(id)) {
                            newList.add(id);
                        }
                    }
                    if (newList.isEmpty()) {
                        return Collections.emptyList();
                    } else {
                        sortedPages.clear();
                        sortedPages.addAll(newList);
                    }
                }
            }
        }
        return sortedPages;
    }

    public static ResultDTO relevance(HashMap<String, Integer> hashMap, List<Integer> list, String site, int offset, int limit) throws IOException {
        ArrayList<Result> results = new ArrayList<>();
        List<Integer> listId;
        int siteId = 0;
        if (site != null) {
            siteId = SpringJdbcConfig.getSiteId(site);
        }
        if (!list.isEmpty()) {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            double maxRelevance = 0;
            ArrayList<Integer> lemmasId = new ArrayList<>();
            ArrayList<Double> absRelevance = new ArrayList<>();
            ArrayList<Double> relRelevance = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                if (siteId != 0) {
                    listId = SpringJdbcConfig.lemmaListId2(entry.getKey(), siteId);
                } else {
                    listId = SpringJdbcConfig.lemmaListId(entry.getKey());
                }
                if (!listId.isEmpty()) {
                    lemmasId.addAll(listId);
                }
            }

            for (Integer pageId : list) {
                double absoluteRelevance = 0;

                StringBuilder query = new StringBuilder();
                query.append(RANK_QUERY).append(pageId).append(RANK_QUERY2);
                for (int i = 0; i < lemmasId.size(); i++) {
                    if (i == 0) {
                        query.append(lemmasId.get(i));
                    } else {
                        query.append(COMMA).append(lemmasId.get(i));
                    }
                }
                query.append(BRACKET);

                List<Float> rankList = SpringJdbcConfig.rankList(query.toString());

                for (Float rank : rankList) {
                    absoluteRelevance += rank;
                }
                if (absoluteRelevance > maxRelevance) {
                    maxRelevance = absoluteRelevance;
                }
                absRelevance.add(absoluteRelevance);
            }

            for (Double absolute : absRelevance) {
                relRelevance.add(absolute / maxRelevance);
            }

            for (int i = 0; i < relRelevance.size(); i++) {
                String uri = SpringJdbcConfig.getString(PATH_QUERY + list.get(i));
                String content = SpringJdbcConfig.getString(CONTENT_QUERY + list.get(i));
                int resSiteId = SpringJdbcConfig.getInt(SITE_ID_QUERY + list.get(i));
                String siteUrl = SpringJdbcConfig.getString(URL_QUERY + resSiteId);
                String siteName = SpringJdbcConfig.getString(NAME_QUERY + resSiteId);

                if (!uri.isEmpty() && !content.isEmpty()) {
                    Document document = Jsoup.parse(content);
                    String title = document.title();
                    String text = title + document.body().text();
                    String[] sentences = text
                            .replaceAll(SENTENCES_REPLACE, SPACE)
                            .split(SENTENCES_REGEX);

                    ArrayList<String> snippet = new ArrayList<>();

                    for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                        for (String sentence : sentences) {
                            String[] words = sentence
                                    .split(SPACE);

                            for (String word : words) {
                                ArrayList<String> normalList = new ArrayList<>();
                                String newSentence = "";
                                String newWord = word.replaceAll(WORDS_REPLACE, SPACE)
                                        .toLowerCase(Locale.ROOT);

                                if (newWord.matches(WORDS_REGEX)) {
                                    String normal = luceneMorph.getNormalForms(newWord).get(0);
                                    if (normal.equals(entry.getKey())) {
                                        newSentence = sentence.replace(word, OPEN_TAG + word + CLOSE_TAG);
                                    }
                                    normalList.add(normal);
                                }
                                if (normalList.contains(entry.getKey())) {
                                    if (!snippet.contains(newSentence)) {
                                        snippet.add(newSentence);
                                    }
                                }
                            }
                        }
                    }
                    results.add(new Result(siteUrl, siteName, uri, title, snippet.toString(), relRelevance.get(i)));
                }
            }
            int resultSize = results.size();
            List<Result> offsetList = new ArrayList<>();
            for (int i = 0; i < resultSize; i++) {
                if (i % offset == 0 && offset <= resultSize) {
                    offsetList.add(results.get(i));
                }
            }
            List<Result> newResults = new ArrayList<>();
            if (limit > offsetList.size()) {
                newResults.addAll(offsetList);
            } else {
                for (int i = 0; i < limit; i++) {
                    newResults.add(offsetList.get(i));
                }
            }
            return new ResultDTO(true, resultSize, newResults);
        }
        return new ResultDTO(false, PAGE_NOT_FOUND);
    }
}
