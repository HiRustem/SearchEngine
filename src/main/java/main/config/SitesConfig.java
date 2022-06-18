package main.config;

import main.model.Site;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Configuration
@ConfigurationProperties(prefix = "sites")
public class SitesConfig {

    private List<Site> list;

    public void setList(List<Site> list) {
        this.list = list;
    }

    public List<Site> getList() {
        return list;
    }
}
