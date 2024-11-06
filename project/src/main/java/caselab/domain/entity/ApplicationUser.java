package caselab.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "id")
@Entity
@Table(name = "application_user", indexes = @Index(columnList = "login"))
public class ApplicationUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String hashedPassword;

    @OneToMany(mappedBy = "applicationUser")
    private List<UserToDocument> usersToDocuments;

    @OneToMany(mappedBy = "applicationUser")
    private List<Signature> signatures;

    @ManyToMany
    @JoinTable(
        name = "global_permission_to_user",
        joinColumns = @JoinColumn(name = "application_user_id", nullable = false),
        inverseJoinColumns = @JoinColumn(name = "global_permission_id", nullable = false)
    )
    private List<GlobalPermission> globalPermissions;
    @OneToMany(mappedBy = "applicationUser")
    private List<Vote> votes;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return globalPermissions.stream().map(gb -> new SimpleGrantedAuthority(gb.getName().name())).toList();
    }

    @Override
    public String getPassword() {
        return hashedPassword;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
