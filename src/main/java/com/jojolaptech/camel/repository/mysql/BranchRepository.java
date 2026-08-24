package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    @Query(
            value = "select b from Branch b join fetch b.company",
            countQuery = "select count(b) from Branch b")
    Page<Branch> findMigratable(Pageable pageable);
}
