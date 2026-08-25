package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.AttTimeTable;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttTimeTableRepository extends JpaRepository<AttTimeTable, Long> {

    @Query("SELECT t FROM AttTimeTable t JOIN FETCH t.company WHERE t.company.id IN :companyIds")
    List<AttTimeTable> findByCompanyIdIn(@Param("companyIds") Collection<Long> companyIds);
}
