package com.jojolaptech.camel.repository.mysql;

import com.jojolaptech.camel.model.mysql.HolidayBranchDepartment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HolidayBranchDepartmentRepository extends JpaRepository<HolidayBranchDepartment, Long> {

    @Query("""
            SELECT h FROM HolidayBranchDepartment h
            JOIN FETCH h.branch
            JOIN FETCH h.attHolidayList
            WHERE h.attHolidayList.id IN :listIds
            """)
    List<HolidayBranchDepartment> findByAttHolidayListIdIn(@Param("listIds") Collection<Long> listIds);
}
