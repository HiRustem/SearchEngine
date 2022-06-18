package main.controller;

import main.repository.StatisticRepository;
import main.dto.*;
import main.model.Site;
import main.service.ExecutorService;
import main.service.SearchService;
import main.config.SitesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SearchController {
    @Autowired
    public SitesConfig sitesConfig;

    @Autowired
    public static boolean isContinue = true;
    @Autowired
    public static boolean isIndexing = false;

    @GetMapping("/api/startIndexing")
    public ResponseEntity<Responses> startIndexing() {
        if (!isIndexing) {
            isContinue = true;
            isIndexing = true;
            List<Site> sites = sitesConfig.getList();
            for (Site site : sites) {
                ExecutorService executor = new ExecutorService();
                new Thread(() -> {
                    try {
                        executor.execute(site.getUrl());
                    } catch (SQLException | IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            return new ResponseEntity<>(new Responses(true), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new Responses(false, "Индексация уже запущена"), HttpStatus.OK);
        }
    }

    @GetMapping("/api/stopIndexing")
    public ResponseEntity<Responses> stopIndexing() {
        if (isIndexing) {
            isContinue = false;
            isIndexing = false;
            return new ResponseEntity<>(new Responses(true), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new Responses(false, "Индексация не запущена"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/indexPage")
    public ResponseEntity<Responses> indexPage(String url) {
        List<Site> sites = sitesConfig.getList();
        List<String> urls = new ArrayList<>();
        for (Site site : sites) {
            urls.add(site.getUrl());
        }

        if (urls.contains(url)) {
            if (!isIndexing) {
                isContinue = true;
                isIndexing = true;
                ExecutorService executor = new ExecutorService();
                new Thread(() -> {
                    try {
                        executor.execute(url);
                    } catch (SQLException | IOException e) {
                        e.printStackTrace();
                    }
                }).start();
                return new ResponseEntity<>(new Responses(true), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new Responses(false, "Индексация уже запущена"), HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(new Responses(false, "Данная страница находится за пределами сайтов, указанных в конфигурационном файле"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api/statistics")
    public ResponseEntity<StatsResultDTO> getStats() {
        return new ResponseEntity<>(StatisticRepository.getStatistics(), HttpStatus.OK);
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<ResultDTO> search(@RequestParam String query, @RequestParam(required = false) String site, @RequestParam(required = false) Integer offset, @RequestParam(required = false) Integer limit) throws IOException {
        if (query.isEmpty()) {
            return new ResponseEntity<>(new ResultDTO(false, "Задан пустой поисковый запрос"), HttpStatus.BAD_REQUEST);
        }
        if (offset == null || offset == 0) {
            offset = 1;
        }
        if (limit == null || limit == 0) {
            limit = 20;
        }
        return new ResponseEntity<>(SearchService.search(query, site, offset, limit), HttpStatus.OK);
    }
}
