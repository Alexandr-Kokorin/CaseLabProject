package caselab.service.department.mapper;

import caselab.controller.department.payload.DepartmentCreateRequest;
import caselab.controller.department.payload.DepartmentResponse;
import caselab.domain.entity.Department;
import java.util.Comparator;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
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
        @Mapping(target = "headEmailOfDepartment", ignore = true)
    })
    Department createRequestToEntity(DepartmentCreateRequest request);

    @Mappings({
        @Mapping(source = "parentDepartment.id", target = "parentDepartment"),
        @Mapping(source = "headEmailOfDepartment", target = "headEmailOfDepartment"),
        @Mapping(target = "childDepartments", expression = "java(mapChildDepartments(department))")
    })
    DepartmentResponse entityToResponse(Department department);

    @Mappings({
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "topDepartment", target = "topDepartment"),
        @Mapping(source = "isActive", target = "isActive"),
        @Mapping(source = "parentDepartment.id", target = "parentDepartment"),
        @Mapping(source = "headEmailOfDepartment", target = "headEmailOfDepartment"),
        @Mapping(target = "childDepartments", ignore = true)
    })
    DepartmentResponse entityToResponseWithoutHierarchy(Department department);

    default List<DepartmentResponse> mapChildDepartments(Department department) {
        return department.getChildDepartments()
            .stream()
            .sorted(Comparator.comparing(Department::getId))
            .map(childDep -> {
                DepartmentResponse response =
                    this.entityToResponse(childDep);
                if (response != null && childDep.getChildDepartments() != null) {
                    response = response.toBuilder()
                        .parentDepartment(null).build();
                }
                return response;
            }).toList();
    }

}
