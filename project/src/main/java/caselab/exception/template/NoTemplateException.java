package caselab.exception.template;

import caselab.exception.base.ApplicationNotFoundException;

public class NoTemplateException extends ApplicationNotFoundException {
    public NoTemplateException(Long docTypeId) {
        super("template.no.template", new Object[] {docTypeId});
    }
}
