package caselab.domain.repository;

import caselab.domain.entity.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByHeadOfDepartmentEmail(String email);
    Optional<Department> findByNameAndParentDepartmentId(String name, Long id);
    Optional<Department> findByName(String name);
    Optional<Department> findById(Long id);
    List<Department> findAllByOrderByIdAsc();

}
