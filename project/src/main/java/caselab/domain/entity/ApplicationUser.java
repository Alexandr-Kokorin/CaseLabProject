package caselab.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "application_user", indexes = @Index(columnList = "login"))
public class ApplicationUser {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String hashedPassword;

    @ManyToMany
    @JoinTable(
        name="user_to_document",
        joinColumns = @JoinColumn(name="document_id", nullable = false),
        inverseJoinColumns = @JoinColumn(name = "user_id", nullable = false)
    )
    private List<Document> documents;
}
