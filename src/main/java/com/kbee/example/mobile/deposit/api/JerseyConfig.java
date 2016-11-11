package com.kbee.example.mobile.deposit.api;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(SimpleCORSFilter.class);
        register(DepositEndpoint.class);
    }

}
