package main.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Responses {

    public Boolean result;
    public String error;

    public Responses(Boolean result) {
        this.result = result;
    }

    public Responses(Boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
