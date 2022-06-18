package main.dto;

import java.util.List;

public class StatisticsDTO {
    public TotalDTO total;
    public List<?> detailed;

    public StatisticsDTO(TotalDTO total, List<?> detailed) {
        this.total = total;
        this.detailed = detailed;
    }
}
