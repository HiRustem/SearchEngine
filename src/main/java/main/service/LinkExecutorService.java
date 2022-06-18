package main.service;

import main.model.MainUrl;
import main.repository.LinkExecutorRepository;

import java.util.concurrent.ForkJoinPool;

public class LinkExecutorService {
    public static void executeLink(String url, MainUrl mainUrl) {
        new ForkJoinPool().invoke(new LinkExecutorRepository(url, mainUrl));
    }
}
