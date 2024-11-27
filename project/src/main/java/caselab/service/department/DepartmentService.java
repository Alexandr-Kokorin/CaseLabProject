package caselab.service.department;

import caselab.controller.department.payload.DepartmentCreateRequest;
import caselab.controller.department.payload.DepartmentCreateResponse;
import caselab.controller.department.payload.DepartmentResponse;
import caselab.controller.department.payload.DepartmentUpdateRequest;
import caselab.controller.department.payload.DepartmentUpdateResponse;
import caselab.controller.users.payload.UserResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Department;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DepartmentRepository;
import caselab.exception.department.DepartmentIllegalParamsException;
import caselab.exception.department.DepartmentMissedParamsException;
import caselab.exception.department.DepartmentSameParentDefined;
import caselab.exception.department.UserAlreadyAHeadOfDepartment;
import caselab.exception.entity.already_exists.DepartmentAlreadyExistsException;
import caselab.exception.entity.not_found.DepartmentNotFoundException;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.service.department.mapper.DepartmentMapper;
import caselab.service.users.mapper.UserMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Stream;
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

    public DepartmentCreateResponse createDepartment(DepartmentCreateRequest request) {

        parentAndTopParamCheck(request.parentDepartment(), request.topDepartment());

       departmentExistsWithNameAndSameParent(request.name(), request.parentDepartment());

       Department parent = null;
       if(request.parentDepartment() != null) {
           parent = findDepartment(request.parentDepartment());
       }

       ApplicationUser user = null;
       if(request.headOfDepartment() != null){
           //user = findUser(request.headOfDepartment());
           checkUserIsNotAHeadOfAnotherDepartment(user.getEmail());
       }

       var department = departmentMapper.createRequestToEntity(request);
           department.setIsActive(true);
           department.setParentDepartment(parent);
           department.setHeadOfDepartment(user);

        depRepo.save(department);

        return departmentMapper.entityToCreateResponse(department);
    }

    public DepartmentResponse getDepartment(Long id, String name) {
        Department dep = findDepartmentWithIdOrName(id, name);

        ApplicationUser user = getHeadOfDepartmentIfExists(dep);

       return DepartmentResponse.builder()
            .id(dep.getId())
            .name(dep.getName())
            .isActive(dep.getIsActive())
            .topDepartment(dep.getTopDepartment())
            .parentDepartment(dep.getParentDepartment() != null ? dep.getParentDepartment().getId() : null)
            .headOfDepartment(user.getDisplayName()+user.getEmail())
            .employees(dep.getEmployees().stream().map(departmentMapper::toEmployeeResponse).toList())
            .build();
    }

    public DepartmentUpdateResponse updateDepartment(Long id, DepartmentUpdateRequest request) {
        Department dep = findDepartment(id);
        Department definedParent = dep.getParentDepartment();
        departmentMapper.patchRequestToEntity(dep, request);
        Department parentToUpdate = null;
        if (request.parentDepartment() != null) {
            parentToUpdate = findDepartment(request.parentDepartment());
            if(parentToUpdate != definedParent){
                dep.setParentDepartment(parentToUpdate);
            } else {
                throw new DepartmentSameParentDefined();
            }
        } else {
            dep.setParentDepartment(definedParent);
        }
        if (request.headOfDepartment() != null) {
           ApplicationUser user = userRepo.findByEmail(request.headOfDepartment())
                .orElseThrow(() -> new UserNotFoundException("User with email: "
                    + request.headOfDepartment()+" not found"));

            if(user != null && (user.getEmail()!=null && !user.getEmail().isEmpty()))
            {
                checkUserIsNotAHeadOfAnotherDepartment(user.getEmail());
                dep.setHeadOfDepartment(user);
                String userInfo = user.getDisplayName()+" ["+user.getPosition()+"]";
            }
        } else {
            dep.setHeadOfDepartment(null);
        }

        if(request.parentDepartment() == null && (request.topDepartment() != null && !request.topDepartment())) {
            throw new DepartmentIllegalParamsException();
            /*throw new IllegalArgumentException("Impossible to proceed. Parent department is " +
                "not specified and top param set as false.");*/
        }
        else if(request.parentDepartment() != null && (request.topDepartment() != null && request.topDepartment())) {
            throw new DepartmentIllegalParamsException(request.topDepartment());
        }

        depRepo.save(dep);
        return departmentMapper.entityToUpdateResponse(dep);
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
        depRepo.deleteById(id);
    }


    private void departmentExistsWithNameAndSameParent(String name, Long parentId) {
        depRepo.findByNameAndParentDepartmentId(name, parentId)
            .orElseThrow(()->new DepartmentAlreadyExistsException(name, parentId));
    }

    private void checkUserIsNotAHeadOfAnotherDepartment(String email) {
       var dep = depRepo.findByHeadOfDepartmentEmail(email);
       if(dep.isPresent()){
           throw new UserAlreadyAHeadOfDepartment(dep.get().getHeadOfDepartment().getDisplayName(), email);
       }
    }

    private void parentAndTopParamCheck(Long parentDepId, Boolean topDep) {
        if (parentDepId != null && (topDep != null && topDep.equals(true))) {
            throw new RuntimeException("Unable to set Top department = true" +
                "parent department is specified");
        }
        else if (parentDepId == null && (topDep == null || topDep.equals(false))) {
            throw new RuntimeException("No parent department nor top department is specified, or also. Must at least one.");
        }
    }

    private Department findDepartment(Long parentDepId) {
        return depRepo.findById(parentDepId)
            .orElseThrow(() -> new DepartmentNotFoundException(parentDepId));
    }

    public List<UserResponse> findUser(String email) {
        ApplicationUser user = userRepo.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));
       return Stream.of(user).map(userMapper::entityToResponse).toList();
    }
    private Department findDepartmentWithIdOrName(Long id, String name) {
        if (id != null) {
            return depRepo.findById(id).orElseThrow(() -> new DepartmentNotFoundException(id));
        } else if (name != null && !name.isEmpty()) {
            return depRepo.findByName(name).orElseThrow(() -> new DepartmentNotFoundException(name));
        } else {
            throw new DepartmentMissedParamsException();
        }
    }

    private ApplicationUser getHeadOfDepartmentIfExists(Department dep) {
        return dep.getHeadOfDepartment() != null ?
            userRepo.findByEmail(dep.getHeadOfDepartment().getEmail())
            .orElseThrow(() -> new UserNotFoundException("No user found with email: "
                + dep.getHeadOfDepartment().getEmail())) : null;
    }
}
