package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "user_lock")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserLockEntity extends BaseAuditEntity {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "user_id")
    private UserEntity user;
    @Enumerated(EnumType.STRING)
    private StatusEnum status;

}
