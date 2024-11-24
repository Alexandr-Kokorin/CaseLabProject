package caselab.service.analytics.export.mapper;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public final class LocalDateEntry {
    private LocalDate category;
    private List<Long> values;
}
