package kafkaeventdriven.kafkaapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.type.descriptor.java.BigIntegerJavaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "user")
@Setter
@Getter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;


    private String lastname;

    private String email;

    private String password;

    private boolean enabled;

    private boolean locked;

    @Column(name = "created_at")
    private LocalDateTime createAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "verification_code")
    private String verificationCode;


    @OneToOne(mappedBy = "userRole",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private Role role;

    @OneToMany(mappedBy = "userAuthority", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Authorities> authorities;






    public User() {
    }

    public User(Integer id, String name, String lastName, String email, String password, boolean enabled, boolean locked, LocalDateTime createAt, LocalDateTime expiredAt, String verificationCode, Role role, List<Authorities> authorities) {
        this.id = id;
        this.name = name;
        this.lastname = lastName;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.locked = locked;
        this.createAt = createAt;
        this.expiredAt = expiredAt;
        this.verificationCode = verificationCode;
        this.role = role;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> grantedAuthorities = this.getAuthorities()
                .stream()
                .map(role->new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toList());

        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    public String getEmail(){
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
