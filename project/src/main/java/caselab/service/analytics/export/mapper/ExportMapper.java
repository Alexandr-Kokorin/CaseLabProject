package caselab.service.analytics.export.mapper;

import caselab.controller.analytics.payload.DocumentTrend;
import caselab.controller.analytics.payload.DocumentTypeDistributionDTO;
import caselab.controller.analytics.payload.DocumentTypesReport;
import caselab.controller.analytics.payload.ReportDocuments;
import caselab.controller.analytics.payload.StageProcessingTimeDTO;
import caselab.controller.analytics.payload.SystemLoadByHourDTO;
import caselab.controller.analytics.payload.UserSignaturesReport;
import caselab.controller.analytics.payload.VotingTimeDistributionDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExportMapper {
    default List<Long> map(Long val) {
        return List.of(val);
    }

    @Mapping(source = "date", target = "category")
    @Mapping(source = "created", target = "values")
    LocalDateEntry map(ReportDocuments dto);

    @Mapping(source = "email", target = "category")
    @Mapping(source = "avgTimeForSigning", target = "values")
    StringEntry map(UserSignaturesReport dto);

    @Mapping(source = "name", target = "category")
    @Mapping(source = "avgTime", target = "values")
    StringEntry map(DocumentTypesReport dto);

    default LocalDateEntry map(DocumentTrend dto) {
        var res = new LocalDateEntry();
        res.setCategory(dto.date());
        res.setValues(List.of(dto.countSigned(), dto.countRefused()));
        return res;
    }

    @Mapping(source = "typeName", target = "category")
    @Mapping(source = "count", target = "values")
    StringEntry map(DocumentTypeDistributionDTO dto);

    @Mapping(source = "stage", target = "category")
    @Mapping(source = "averageTimeInMinutes", target = "values")
    StringEntry map(StageProcessingTimeDTO dto);

    @Mapping(source = "timeRange", target = "category")
    @Mapping(source = "count", target = "values")
    StringEntry map(VotingTimeDistributionDTO dto);

    @Mapping(source = "startTime", target = "category")
    @Mapping(source = "count", target = "values")
    StringEntry map(SystemLoadByHourDTO dto);
}
