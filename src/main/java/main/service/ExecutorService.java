package main.service;

import main.repository.ExecutorRepository;

import java.io.IOException;
import java.sql.SQLException;

public class ExecutorService {
    public void execute(String url) throws SQLException, IOException {
        ExecutorRepository.start(url);
    }
}
