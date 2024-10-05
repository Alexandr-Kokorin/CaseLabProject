package caselab.domain.entity.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiError {
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
}
