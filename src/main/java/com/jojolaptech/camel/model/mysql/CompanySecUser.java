package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/** Legacy Grails join: company admins ↔ sec_user ({@code company_sec_user}). */
@Entity
@IdClass(CompanySecUser.Pk.class)
@Table(name = "companySecUser")
@Getter
@Setter
public class CompanySecUser implements Serializable {

    @Id
    @Column(name = "companyAdminsId")
    private Long companyId;

    @Id
    @Column(name = "secUserId")
    private Long secUserId;

    @Getter
    @Setter
    public static class Pk implements Serializable {
        private Long companyId;
        private Long secUserId;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pk that)) {
                return false;
            }
            return Objects.equals(companyId, that.companyId) && Objects.equals(secUserId, that.secUserId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(companyId, secUserId);
        }
    }
}
