package caselab.service.signature;

import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.Signature;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SignatureMapper {
    Signature requestToEntity(SignatureCreateRequest request);

    @Mapping(target = "email", source = "applicationUser.email")
    @Mapping(target = "documentVersionId", source = "documentVersion.id")
    SignatureResponse entityToResponse(Signature signature);
}
