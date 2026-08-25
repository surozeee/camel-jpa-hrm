package com.jojolaptech.camel.config;

import com.jojolaptech.camel.service.MasterDataSeedService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final MasterDataSeedService masterDataSeedService;

    @Override
    public void run(String... args) {
        int seeded = masterDataSeedService.seedAll();
        log.info("Platform master seed ensured {} records", seeded);
    }
}
