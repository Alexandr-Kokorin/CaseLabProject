package caselab.domain.repository;

import caselab.domain.entity.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByHeadEmailOfDepartment(String email);

    @Query("SELECT d FROM Department d WHERE d.name = :name AND d.parentDepartment.id = :id")
    Optional<Department> findByNameAndParentDepartmentId(@Param("name") String name, @Param("id") Long id);

    List<Department> findAllByOrderByIdAsc();

}
