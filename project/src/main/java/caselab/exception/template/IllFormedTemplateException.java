package caselab.exception.template;

import caselab.exception.base.ApplicationInternalServerErrorException;

public class IllFormedTemplateException extends ApplicationInternalServerErrorException {
    public IllFormedTemplateException(Long documentTypeId) {
        super("template.ill.formed.template", new Object[] {documentTypeId});
    }
}
