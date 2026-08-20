package com.jojolaptech.camel.route;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ImportRouteBuilder extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(ImportRouteBuilder.class);

    
    // Optimized page size: balance between memory usage and database round trips
    // 100 records per page reduces queries by 20x compared to 5, while keeping memory manageable
    private static final int PAGE_SIZE = 100;
    
    // Throttle delay between migrations to allow GC and prevent memory buildup
    private static final int MIGRATION_THROTTLE_MS = 1000;

    @Override
    public void configure() {

        errorHandler(defaultErrorHandler()
                .maximumRedeliveries(3)
                .redeliveryDelay(2000));

        
    }
}


