package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Entity
@IdClass(SecUserSecRole.Pk.class)
@Table(name = "secUserSecRole")
@Getter
@Setter
public class SecUserSecRole implements Serializable {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secUser_id", nullable = false)
    private SecUser secUser;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secRole_id", nullable = false)
    private SecRole secRole;

    @Getter
    @Setter
    public static class Pk implements Serializable {
        private Long secRole;
        private Long secUser;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pk that)) {
                return false;
            }
            return Objects.equals(secRole, that.secRole) && Objects.equals(secUser, that.secUser);
        }

        @Override
        public int hashCode() {
            return Objects.hash(secRole, secUser);
        }
    }
}
