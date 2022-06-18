package main.dto;

public class StatsResultDTO {
    public boolean result;
    public StatisticsDTO statistics;

    public StatsResultDTO(boolean result, StatisticsDTO statistics) {
        this.result = result;
        this.statistics = statistics;
    }
}
