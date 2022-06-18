package main.service;

import main.model.MainUrl;
import main.repository.LemmaRepository;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;


public class LemmaService {
    private static final String REGEX = ".*ПРЕДЛ|.*СОЮЗ.*|.*МС-П.*|.*МЕЖД|.*ЧАСТ|.*МС.*";
    private static final String WORDS_REPLACE = "[^А-Яа-я\\s]";
    private static final String WORDS_REGEX = "[А-Яа-я]{2,}";
    private static final String TITLE = "title";
    private static final String BODY = "body";
    private static final String SPACE = " ";

    public static void getLemma(String title, String body, String url, MainUrl mainUrl) throws IOException, SQLException {
        HashSet<String> lemmas = new HashSet<>();
        HashMap<String, Integer> titleRepeats = new HashMap<>();
        HashMap<String, Integer> bodyRepeats = new HashMap<>();
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();

        String[] titleWords = title
                .replaceAll(WORDS_REPLACE, SPACE)
                .toLowerCase(Locale.ROOT)
                .split(SPACE);

        for (String word : titleWords) {
            if (word.matches(WORDS_REGEX)) {
                String normal = luceneMorph.getNormalForms(word).get(0);
                String morph = luceneMorph.getMorphInfo(normal).get(0);
                if (!morph.matches(REGEX)) {
                    lemmas.add(normal);
                    if (!titleRepeats.containsKey(normal)) {
                        titleRepeats.put(normal, 1);
                    } else {
                        titleRepeats.put(normal, titleRepeats.get(normal) + 1);
                    }
                }
            }
        }

        String[] bodyWords = body
                .replaceAll(WORDS_REPLACE, SPACE)
                .toLowerCase(Locale.ROOT)
                .split(SPACE);

        for (String word : bodyWords) {
            if (word.matches(WORDS_REGEX)) {
                String normal = luceneMorph.getNormalForms(word).get(0);
                String morph = luceneMorph.getMorphInfo(normal).get(0);
                if (!morph.matches(REGEX)) {
                    lemmas.add(normal);
                    if (!bodyRepeats.containsKey(normal)) {
                        bodyRepeats.put(normal, 1);
                    } else {
                        bodyRepeats.put(normal, bodyRepeats.get(normal) + 1);
                    }
                }
            }
        }

        LemmaRepository.addInLemmas(lemmas, mainUrl);
        LemmaRepository.addInIndex(titleRepeats, url, TITLE, mainUrl);
        LemmaRepository.addInIndex(bodyRepeats, url, BODY, mainUrl);
    }
}



