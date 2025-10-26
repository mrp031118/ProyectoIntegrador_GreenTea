package com.example.demo.entity.usuarios;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;



@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "El username es obligatorio")
    @Pattern(regexp="^[\\w.-]+@gtcoffee\\.com$", message="El username debe ser un correo @gtcoffee.com")
    private String username;

    @Column(nullable = false)
    private String contra;
    
    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp="^\\d{9}$", message="El teléfono debe tener 9 dígitos")
    private String telefono;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true; // control interno para autenticación

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    // Se ejecuta automáticamente antes de insertar
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
