package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttParams;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttParamsRepository extends JpaRepository<AttParams, Long> {

    @Query(
            value = "SELECT a FROM AttParams a JOIN FETCH a.company",
            countQuery = "SELECT count(a) FROM AttParams a")
    Page<AttParams> findMigratable(Pageable pageable);

    @Query("SELECT a FROM AttParams a JOIN FETCH a.company WHERE a.company.id IN :companyIds")
    List<AttParams> findByCompanyIdIn(@Param("companyIds") Collection<Long> companyIds);
}
