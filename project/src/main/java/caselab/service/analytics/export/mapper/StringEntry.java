package caselab.service.analytics.export.mapper;

import java.util.List;
import lombok.Data;

@Data
public final class StringEntry {
    private String category;
    private List<Long> values;
}
