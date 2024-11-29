package caselab.service.department.mapper;

import caselab.controller.department.payload.DepartmentCreateRequest;
import caselab.controller.department.payload.DepartmentCreateResponse;
import caselab.controller.department.payload.DepartmentResponse;
import caselab.controller.department.payload.DepartmentUpdateRequest;
import caselab.controller.department.payload.DepartmentUpdateResponse;
import caselab.controller.department.payload.EmployeeResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Department;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DepartmentMapper {

    @Mappings({
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "topDepartment", target = "topDepartment"),
        @Mapping(target = "parentDepartment", ignore = true),
        @Mapping(target = "headOfDepartment", ignore = true)
    })
    Department createRequestToEntity(DepartmentCreateRequest request);

    @Mappings({
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "topDepartment", target = "topDepartment"),
        @Mapping(source = "isActive", target = "isActive")
    })
    DepartmentCreateResponse entityToCreateResponse(Department department);

    @Mappings({
        @Mapping(source = "parentDepartment.id", target = "parentDepartment"),
        @Mapping(source = "headOfDepartment.email", target = "headOfDepartment"),
        @Mapping(target = "childDepartments", expression = "java(mapChildDepartments(department))"),
        @Mapping(target = "employees", qualifiedByName = "mapEmployees")
    })
    DepartmentResponse entityToResponse(Department department);

    @Mappings({
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "topDepartment", target = "topDepartment"),
        @Mapping(source = "isActive", target = "isActive"),
        @Mapping(source = "parentDepartment.id", target = "parentDepartment"),
        @Mapping(source = "headOfDepartment.email", target = "headOfDepartment")
    })
    DepartmentUpdateResponse entityToUpdateResponse(Department department);

    @Mappings({
        @Mapping(source = "displayName", target = "name"),
        @Mapping(source = "email", target = "email"),
        @Mapping(source = "isWorking", target = "isWorking"),
        @Mapping(source = "position", target = "position", ignore = true)
    })
    EmployeeResponse toEmployeeResponse(ApplicationUser applicationUser);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "topDepartment", target = "topDepartment"),
        @Mapping(source = "isActive", target = "isActive"),
        @Mapping(source = "parentDepartment", target = "parentDepartment.id"),
        @Mapping(source = "headOfDepartment", target = "headOfDepartment.email", ignore = true),
    })
    void patchRequestToEntity(@MappingTarget Department response, DepartmentUpdateRequest request);

    default List<DepartmentResponse> mapChildDepartments(Department department) {
        return department.getChildDepartments()
            .stream()
            .sorted(Comparator.comparing(Department::getId)) //
            .map(childDep -> {
                DepartmentResponse response =
                    this.entityToResponse(childDep); // для икслючения perent_department_id в ответе
                if (response != null && childDep.getChildDepartments() != null) {
                    response = response.toBuilder()
                        .parentDepartment(null).build();
                }
                return response;
            }).toList();
    }

    @Named("mapEmployees")
    default List<EmployeeResponse> mapEmployees(List<ApplicationUser> employees) {
        return employees.stream()
            .map(this::toEmployeeResponse)
            .collect(Collectors.toList());
    }

}
