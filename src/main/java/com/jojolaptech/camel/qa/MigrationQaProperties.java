package com.jojolaptech.camel.qa;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "migration.qa")
public class MigrationQaProperties {

    /** Run row-count QA automatically after the master migration route completes. */
    private boolean enabled = true;

    /** When true, log QA failures at ERROR and set exchange property migrationQaPassed=false. */
    private boolean failOnMismatch = false;
}
