package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.Requestmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestmapRepository extends JpaRepository<Requestmap, Long> {}
