package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.UserLicense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLicenseRepository extends JpaRepository<UserLicense, Long> {

    @Query(
            value = """
                    SELECT u FROM UserLicense u
                    JOIN FETCH u.company
                    """,
            countQuery = "SELECT count(u) FROM UserLicense u")
    Page<UserLicense> findMigratable(Pageable pageable);
}
