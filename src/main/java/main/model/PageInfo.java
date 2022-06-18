package main.model;


public class PageInfo {
    public String url;
    public String name;
    public String status;
    public String statusTime;
    public String error;
    public int pages;
    public int lemmas;

    public PageInfo(String url, String name, String status, String statusTime, String error, int pages, int lemmas) {
        this.url = url;
        this.name = name;
        this.status = status;
        this.statusTime = statusTime;
        this.error = error;
        this.pages = pages;
        this.lemmas = lemmas;
    }

}
