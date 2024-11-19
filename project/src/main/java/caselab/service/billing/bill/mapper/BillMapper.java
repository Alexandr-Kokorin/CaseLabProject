package caselab.service.billing.bill.mapper;

import caselab.controller.billing.bill.payload.BillResponse;
import caselab.domain.entity.Bill;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BillMapper {
    BillResponse toResponse(Bill bill);
}
