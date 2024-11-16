package caselab.service.billing.tariff.mapper;


import caselab.controller.billing.tariff.payload.TariffResponse;
import caselab.domain.entity.Tariff;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TariffMapper {
    TariffResponse entityToResponse(Tariff tariff);
}
