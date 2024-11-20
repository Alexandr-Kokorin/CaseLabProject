package caselab.multitenancy.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public abstract class TenantAwareEntity implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TenantId
    @Size(max = 128)
    @Column(name = "tenant_id")
    private String tenantId;
}
