package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "date_time_format")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DateTimeFormatEntity extends BaseAuditEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "date_format", nullable = false)
    private String dateFormat;

    @Column(name = "time_format", nullable = false)
    private String timeFormat;

    @Column(name = "date_time_format", nullable = false)
    private String dateTimeFormat;

    private String example;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}
