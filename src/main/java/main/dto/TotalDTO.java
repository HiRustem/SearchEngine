package main.dto;

public class TotalDTO {
    public int sites, pages, lemmas;
    public boolean indexStatus;

    public TotalDTO(){}

    public TotalDTO(int sites, int pages, int lemmas, boolean indexStatus) {
        this.sites = sites;
        this.pages = pages;
        this.lemmas = lemmas;
        this.indexStatus = indexStatus;
    }

}
