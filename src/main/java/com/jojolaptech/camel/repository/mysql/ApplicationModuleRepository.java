package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.ApplicationModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationModuleRepository extends JpaRepository<ApplicationModule, Long> {

    @Query(
            value = "SELECT a FROM ApplicationModule a",
            countQuery = "SELECT count(a) FROM ApplicationModule a")
    Page<ApplicationModule> findMigratable(Pageable pageable);
}
