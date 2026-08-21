package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nepali_calendar")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NepaliCalendarEntity extends BaseAuditEntity {
    @Column(unique = true)
    private Integer year;
    private Integer baishakhDay;
    private Integer jesthaDay;
    private Integer asarDay;
    private Integer shrawanDay;
    private Integer bhadraDay;
    private Integer ashojDay;
    private Integer kartikDay;
    private Integer mangsirDay;
    private Integer poushDay;
    private Integer maghDay;
    private Integer falgunDay;
    private Integer chaitraDay;
}
