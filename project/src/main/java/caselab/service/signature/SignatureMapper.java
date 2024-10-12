package caselab.service.signature;

import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.Signature;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {SignatureMapper.class})
public interface SignatureMapper {

    SignatureResponse entityToResponse(Signature signature);
}
