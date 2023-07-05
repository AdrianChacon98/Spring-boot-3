package kafkaeventdriven.kafkaapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.type.descriptor.java.BigIntegerJavaType;


@Entity
@Table(name = "authority")
@Setter
@Getter
public class Authorities {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "authority_name")
    private String authorityName;

    @ManyToOne
    @JoinColumn(name = "user_authority_id")
    private User userAuthority;


    public Authorities(String authorityName, User userAuthority){
        this.authorityName=authorityName;
        this.userAuthority=userAuthority;
    }

    public Authorities(){}


}
