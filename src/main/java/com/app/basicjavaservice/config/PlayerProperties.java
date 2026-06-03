package com.app.basicjavaservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "player")
public class PlayerProperties {
    private int pageLimit;

    public int getPageLimit() {
        return pageLimit;
    }
}
