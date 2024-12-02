package caselab.exception.template;

import caselab.exception.base.ApplicationNotFoundException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class NoTemplateException extends ApplicationNotFoundException {
    public NoTemplateException(Long docTypeId) {
        super("template.no.template", new Object[] {docTypeId});
    }
}
