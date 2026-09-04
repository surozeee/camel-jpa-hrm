package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.VacancyNewspaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VacancyNewspaperRepository extends JpaRepository<VacancyNewspaper, Long> {

    @Query(
            value = "SELECT vn FROM VacancyNewspaper vn JOIN FETCH vn.vacancy",
            countQuery = "SELECT count(vn) FROM VacancyNewspaper vn")
    Page<VacancyNewspaper> findMigratable(Pageable pageable);
}
