package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CompanySettingParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanySettingParamsRepository extends JpaRepository<CompanySettingParams, Long> {

    @Query(
            value = """
                    SELECT c FROM CompanySettingParams c
                    JOIN FETCH c.company
                    """,
            countQuery = "SELECT count(c) FROM CompanySettingParams c")
    Page<CompanySettingParams> findMigratable(Pageable pageable);
}
