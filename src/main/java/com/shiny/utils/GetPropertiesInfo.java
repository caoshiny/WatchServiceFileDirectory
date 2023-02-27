package com.shiny.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "file")
public class GetPropertiesInfo {
    private Boolean isneedcopy;

    private List<String> paths;

    public Boolean getIsneedcopy() {
        return isneedcopy;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setIsneedcopy(Boolean isneedcopy) {
        this.isneedcopy = isneedcopy;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}
