package caselab.service.department;

import caselab.controller.department.payload.DepartmentCreateRequest;
import caselab.controller.department.payload.DepartmentCreateResponse;
import caselab.controller.department.payload.DepartmentResponse;
import caselab.controller.department.payload.DepartmentUpdateRequest;
import caselab.controller.department.payload.DepartmentUpdateResponse;
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
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class DepartmentService {

    private final ApplicationUserRepository userRepo;
    private final DepartmentRepository depRepo;
    private final DepartmentMapper departmentMapper;

    public DepartmentCreateResponse createDepartment(DepartmentCreateRequest request) {
       var department = departmentMapper.createRequestToEntity(request);
        department.setIsActive(true);
        parentAndTopParamCheckToCreate(request.parentDepartment(), request.topDepartment());

        departmentExistsWithNameAndSameParent(request.name(), request.parentDepartment());

       if(request.parentDepartment() != null) {
           Department parent = findDepartmentByIdOrName(request.parentDepartment(), null);
           department.setParentDepartment(parent);
       }

       if(request.headOfDepartment() != null){
           ApplicationUser user = findUserByEmail(request.headOfDepartment());
           checkUserIsNotAHeadOfAnotherDepartment(user.getEmail());
           department.setHeadOfDepartment(user);
       }
        depRepo.save(department);
        return departmentMapper.entityToCreateResponse(department);
    }

    public DepartmentResponse getDepartment(Long id, String name) {
        Department dep = findDepartmentByIdOrName(id, name);
        ApplicationUser user = getHeadOfDepartmentIfExists(dep);
        String userInfo = user != null ? user.getDisplayName() + " [" + user.getPosition() + "]" : "No head of department";
        return DepartmentResponse.builder()
            .id(dep.getId())
            .name(dep.getName())
            .isActive(dep.getIsActive())
            .topDepartment(dep.getTopDepartment())
            .parentDepartment(dep.getParentDepartment() != null ? dep.getParentDepartment().getId() : null)
            .headOfDepartment(userInfo)
            .employees(dep.getEmployees().stream().map(departmentMapper::toEmployeeResponse).toList())
            .build();
    }

    public DepartmentUpdateResponse updateDepartment(Long id, DepartmentUpdateRequest request) {
        Department dep = findDepartmentByIdOrName(id, null);
        Department definedParent = dep.getParentDepartment();
        departmentMapper.patchRequestToEntity(dep, request);
        if (request.parentDepartment() != null) {
            Department parentToUpdate = findDepartmentByIdOrName(request.parentDepartment(), null);
            if(parentToUpdate != definedParent){
                dep.setParentDepartment(parentToUpdate);
            } else {
                throw new DepartmentSameParentDefined();
            }
        } else {
            dep.setParentDepartment(definedParent);
        }
        if (request.headOfDepartment() != null) {
           ApplicationUser user = findUserByEmail(request.headOfDepartment());
            if(user != null && (user.getEmail()!=null && !user.getEmail().isEmpty()))
            {
                checkUserIsNotAHeadOfAnotherDepartment(user.getEmail());
                dep.setHeadOfDepartment(user);
            }
        } else {
            dep.setHeadOfDepartment(null);
        }

        parentAndTopParamCheckToUpdate(request.parentDepartment(), request.topDepartment());

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
           .ifPresent((dep)-> {throw new DepartmentAlreadyExistsException();});
    }

    private void checkUserIsNotAHeadOfAnotherDepartment(String email) {
       var dep = depRepo.findByHeadOfDepartmentEmail(email);
       if(dep.isPresent()){
           throw new UserAlreadyAHeadOfDepartment(dep.get().getHeadOfDepartment().getDisplayName(), email);
       }
    }

    private void parentAndTopParamCheckToCreate(Long parentDepId, Boolean topDep) {
        if (parentDepId != null && (topDep != null && !topDep)) {
            throw new DepartmentIllegalParamsException(topDep);
        }
        else if (parentDepId == null && (topDep == null || !topDep)) {
            throw new DepartmentIllegalParamsException();
        }
    }

    private void parentAndTopParamCheckToUpdate(Long parentDepId, Boolean topDep) {
        if(parentDepId == null && (topDep != null && !topDep)) {
            throw new DepartmentIllegalParamsException();
        }
        else if(parentDepId != null && (topDep != null && topDep)) {
            throw new DepartmentIllegalParamsException(topDep);
        }
    }

    private Department findDepartmentByIdOrName(Long depId, String name) {
        if (depId != null) {
            return depRepo.findById(depId).orElseThrow(() -> new DepartmentNotFoundException(depId));
        } else if (name != null && !name.isEmpty()) {
            return depRepo.findByName(name).orElseThrow(() -> new DepartmentNotFoundException(name));
        } else throw new DepartmentMissedParamsException();
    }

    public ApplicationUser findUserByEmail(String email) {
        return userRepo.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User with email "
                + email +" not found"));
    }

    private ApplicationUser getHeadOfDepartmentIfExists(Department dep) {
        return dep.getHeadOfDepartment() != null ?
           findUserByEmail(dep.getHeadOfDepartment().getEmail()) : null;
    }
}
