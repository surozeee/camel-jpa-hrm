package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.ChannelEnum;
import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import com.jojolaptech.camel.model.postgres.enums.LanguageEnum;
import com.jojolaptech.camel.model.postgres.user.enums.GenderEnum;
import com.jojolaptech.camel.model.postgres.user.enums.SalutationEnum;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "user_detail")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserDetailEntity extends BaseAuditEntity {

    @Enumerated(EnumType.STRING)
    private SalutationEnum salutation;

    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    private String name;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    private String phoneNumber;

    /** URL of user profile photo (stored in bucket under user/{userId}). */
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    private LanguageEnum language;

    private boolean enable2FA;

    @Enumerated(EnumType.STRING)
    private CountryEnum country;

    @Enumerated(EnumType.STRING)
    private ChannelEnum notifyTo;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
