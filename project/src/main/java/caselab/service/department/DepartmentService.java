package caselab.service.department;

import caselab.controller.department.payload.DepartmentCreateRequest;
import caselab.controller.department.payload.DepartmentResponse;
import caselab.controller.department.payload.DepartmentUpdateRequest;
import caselab.controller.department.payload.EmployeeRequest;
import caselab.controller.users.payload.UserResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Department;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DepartmentRepository;
import caselab.exception.department.DepartmentIllegalParamsException;
import caselab.exception.department.UserAlreadyAHeadOfDepartment;
import caselab.exception.department.UserAlreadyWorksInAnotherDepartment;
import caselab.exception.entity.already_exists.DepartmentAlreadyExistsException;
import caselab.exception.entity.not_found.DepartmentNotFoundException;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.service.department.mapper.DepartmentMapper;
import caselab.service.users.mapper.UserMapper;
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
    private final UserMapper userMapper;

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

        var user = findUserByEmail(request.headEmailOfDepartment());
        checkUserIsNotAHeadOfAnotherDepartment(user.getEmail());
        department.setHeadEmailOfDepartment(user.getEmail());

        var savedDep = depRepo.save(department);
        user.setIsWorking(true);
        user.setDepartment(savedDep);
        userRepo.save(user);
        return departmentMapper.entityToResponseWithNotHierarchy(savedDep);
    }

    public DepartmentResponse getDepartment(Long id) {
        var dep = findDepartmentById(id);
        return departmentMapper.entityToResponseWithNotHierarchy(dep);
    }

    public DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request) {
        var dep = findDepartmentById(id);

        if (request.headEmailOfDepartment() != null) {
            checkUserIsNotAHeadOfAnotherDepartmentForUpdate(id, request.headEmailOfDepartment());

            var lastHead = findUserByEmail(dep.getHeadEmailOfDepartment());
            lastHead.setDepartment(null);
            lastHead.setIsWorking(false);
            userRepo.save(lastHead);

            var headForUpdating = findUserByEmail(request.headEmailOfDepartment());
            headForUpdating.setIsWorking(true);
            headForUpdating.setDepartment(dep);
            userRepo.save(headForUpdating);
            dep.setHeadEmailOfDepartment(headForUpdating.getEmail());
            depRepo.save(dep);
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
        var departments = depRepo.findAllByOrderByIdAsc();

        var childDepartmentIds = departments.stream()
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
        var dep = findDepartmentById(id);
        var headOfDep = findUserByEmail(dep.getHeadEmailOfDepartment());
        headOfDep.setIsWorking(false);
        userRepo.save(headOfDep);
        depRepo.deleteById(id);
    }

    public UserResponse addEmployeeToDepartment(Long departmentId, EmployeeRequest addEmployeeRequest) {
        var dep = findDepartmentById(departmentId);
        var userForAdding = findUserByEmail(addEmployeeRequest.userEmail());

        if (userForAdding.getDepartment() != null) {
            throw new UserAlreadyWorksInAnotherDepartment(addEmployeeRequest.userEmail());
        }

        userForAdding.setIsWorking(true);
        userForAdding.setDepartment(dep);
        var employee = userRepo.save(userForAdding);
        return userMapper.entityToResponse(employee);
    }

    public UserResponse deleteEmployeeFromDepartment(EmployeeRequest addEmployeeRequest) {
        var userForDeletingDepartment = findUserByEmail(addEmployeeRequest.userEmail());

        userForDeletingDepartment.setIsWorking(false);
        userForDeletingDepartment.setDepartment(null);

        var employee = userRepo.save(userForDeletingDepartment);
        return userMapper.entityToResponse(employee);
    }

    private void departmentExistsWithNameAndSameParent(String name, Long parentId) {
        depRepo.findByNameAndParentDepartmentId(name, parentId)
            .ifPresent((dep) -> {
                throw new DepartmentAlreadyExistsException(name, parentId);
            });
    }

    private void checkUserIsNotAHeadOfAnotherDepartment(String email) {
        var dep = depRepo.findByHeadEmailOfDepartment(email);

        if (dep.isPresent()) {
            throw new UserAlreadyAHeadOfDepartment(email);
        }
    }

    private void checkUserIsNotAHeadOfAnotherDepartmentForUpdate(Long id, String email) {
        var dep = depRepo.findByHeadEmailOfDepartment(email);

        if (dep.isPresent()) {
            if (!Objects.equals(dep.get().getId(), id)) {
                throw new UserAlreadyAHeadOfDepartment(email);
            }
        }
    }

    private Department findDepartmentById(Long departmentId) {
        return depRepo.findById(departmentId)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
    }

    private ApplicationUser findUserByEmail(String email) {
        return userRepo.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));
    }

    public List<UserResponse> getAllEmployeesForDepartment(Long id) {
        var dep = findDepartmentById(id);

        return dep.getEmployees().stream()
            .map(userMapper::entityToResponse)
            .toList();
    }
}
