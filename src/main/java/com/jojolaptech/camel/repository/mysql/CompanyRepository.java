package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
}
