package caselab.multitenancy;

import java.util.Map;
import java.util.Objects;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

@Component
public class CurrentTenantIdentifierResolverImpl
    implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    private final String DEFAULT_TENANT = "default";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        return Objects.requireNonNullElse(tenantId, DEFAULT_TENANT);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
