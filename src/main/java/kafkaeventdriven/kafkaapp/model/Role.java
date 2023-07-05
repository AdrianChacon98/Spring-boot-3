package kafkaeventdriven.kafkaapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.type.descriptor.java.BigIntegerJavaType;


@Entity
@Table(name = "role")
@Getter
@Setter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String roleName;

    @OneToOne
    @JoinColumn(name = "user_role_id",unique = true)
    private User userRole;


}
