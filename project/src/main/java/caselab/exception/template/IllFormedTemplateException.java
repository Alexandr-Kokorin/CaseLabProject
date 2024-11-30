package caselab.exception.template;

import caselab.exception.base.ApplicationInternalServerErrorException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class IllFormedTemplateException extends ApplicationInternalServerErrorException {
    public IllFormedTemplateException(Long documentTypeId) {
        super("template.ill.formed.template", new Object[] {documentTypeId});
    }
}
