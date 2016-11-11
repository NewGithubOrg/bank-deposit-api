package com.kbee.example.mobile.deposit.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

@SpringBootApplication
public class MobileDepositApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MobileDepositApplication.class);
    }

    public static void main(String[] args) {
        new MobileDepositApplication().configure(
                new SpringApplicationBuilder(MobileDepositApplication.class)).run(args);
    }


}
