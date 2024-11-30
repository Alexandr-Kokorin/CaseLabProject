package caselab.service.department;

import caselab.controller.department.payload.DepartmentCreateRequest;
import caselab.controller.department.payload.DepartmentResponse;
import caselab.controller.department.payload.DepartmentUpdateRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Department;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DepartmentRepository;
import caselab.exception.department.DepartmentIllegalParamsException;
import caselab.exception.department.UserAlreadyAHeadOfDepartment;
import caselab.exception.entity.already_exists.DepartmentAlreadyExistsException;
import caselab.exception.entity.not_found.DepartmentNotFoundException;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.service.department.mapper.DepartmentMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class DepartmentService {

    private final ApplicationUserRepository userRepo;
    private final DepartmentRepository depRepo;
    private final DepartmentMapper departmentMapper;

    public DepartmentResponse createDepartment(DepartmentCreateRequest request) {
        var department = departmentMapper.createRequestToEntity(request);
        department.setIsActive(true);

        departmentExistsWithNameAndSameParent(request.name(), request.parentDepartment());

        if (!request.topDepartment()) {
            if (request.parentDepartment() != null) {
                var parent = findDepartmentById(request.parentDepartment());
                department.setParentDepartment(parent);
            } else {
                throw new DepartmentIllegalParamsException();
            }
        }

        var user = findUserByEmail(request.headOfDepartment());
        checkUserIsNotAHeadOfAnotherDepartment(user.getEmail());
        department.setHeadOfDepartment(user);

        var savedDep = depRepo.save(department);
        return departmentMapper.entityToResponseWithNotHierarchy(savedDep);
    }

    public DepartmentResponse getDepartment(Long id) {
        var dep = findDepartmentById(id);
        return departmentMapper.entityToResponseWithNotHierarchy(dep);
    }

    public DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request) {
        var dep = findDepartmentById(id);

        if (request.headOfDepartment() != null) {
            checkUserIsNotAHeadOfAnotherDepartmentForUpdate(id, request.headOfDepartment());
            var user = findUserByEmail(request.headOfDepartment());
            dep.setHeadOfDepartment(user);
        }

        dep.setIsActive(request.isActive() != null && request.isActive());
        dep.setName(request.name() != null ? request.name() : dep.getName());

        if (!dep.getTopDepartment() && request.parentDepartment() != null) {
            var parentToUpdate = findDepartmentById(request.parentDepartment());
            dep.setParentDepartment(parentToUpdate);
        }

        depRepo.save(dep);
        return departmentMapper.entityToResponseWithNotHierarchy(dep);
    }

    public List<DepartmentResponse> getAllDepartmentsHierarchy() {
        List<Department> departments = depRepo.findAllByOrderByIdAsc();

        List<Long> childDepartmentIds = departments.stream()
            .flatMap(dep -> dep.getChildDepartments().stream())
            .map(Department::getId)
            .toList();

        departments = departments.stream()
            .filter(dep -> !childDepartmentIds.contains(dep.getId()))
            .toList();

        return departments.stream()
            .map(departmentMapper::entityToResponse)
            .toList();
    }

    public void deleteDepartment(Long id) {
        findDepartmentById(id);
        depRepo.deleteById(id);
    }

    private void departmentExistsWithNameAndSameParent(String name, Long parentId) {
        depRepo.findByNameAndParentDepartmentId(name, parentId)
            .ifPresent((dep) -> {
                throw new DepartmentAlreadyExistsException();
            });
    }

    private void checkUserIsNotAHeadOfAnotherDepartment(String email) {
        var dep = depRepo.findByHeadOfDepartmentEmail(email);

        if (dep.isPresent()) {
            throw new UserAlreadyAHeadOfDepartment(dep.get().getHeadOfDepartment().getDisplayName(), email);
        }
    }

    private void checkUserIsNotAHeadOfAnotherDepartmentForUpdate(Long id, String email) {
        var dep = depRepo.findByHeadOfDepartmentEmail(email);

        if (dep.isPresent()) {
            if (!Objects.equals(dep.get().getId(), id)) {
                throw new UserAlreadyAHeadOfDepartment(dep.get().getHeadOfDepartment().getDisplayName(), email);
            }
        }
    }

    private Department findDepartmentById(Long departmentId) {
        return depRepo.findById(departmentId)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
    }

    public ApplicationUser findUserByEmail(String email) {
        return userRepo.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));
    }
}
