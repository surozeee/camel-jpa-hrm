package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.CostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CostTypeRepository extends JpaRepository<CostType, Long> {

    @Query(
            value = """
                    SELECT c FROM CostType c
                    """,
            countQuery = """
                    SELECT count(c) FROM CostType c
                    """)
    Page<CostType> findMigratable(Pageable pageable);
}
