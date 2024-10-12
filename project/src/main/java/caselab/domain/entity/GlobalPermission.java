package caselab.domain.entity;

import caselab.domain.entity.enums.GlobalPermissionName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "global_permission")
public class GlobalPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GlobalPermissionName name;

    @ManyToMany
    @JoinTable(
        name = "global_permission_to_user",
        joinColumns = @JoinColumn(name = "global_permission_id", nullable = false),
        inverseJoinColumns = @JoinColumn(name = "application_user_id ", nullable = false)
    )
    private List<ApplicationUser> applicationUsers;
}
