package main.service;

import main.dto.ResultDTO;
import main.repository.SearchRepository;

import java.io.IOException;

public class SearchService {
    public static ResultDTO search(String query, String site, Integer offset, Integer limit) throws IOException {
        return SearchRepository.results(query, site, offset, limit);
    }
}
