package main.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import main.model.Result;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResultDTO {

    public boolean result;
    public Integer count;
    public List<Result> data;
    public String error;

    public ResultDTO(boolean result, int count, List<Result> list) {
        this.result = result;
        this.count = count;
        this.data = list;
    }

    public ResultDTO(boolean result, String error) {
        this.result = result;
        this.error = error;
        this.count = null;
    }
}
