package kafkaeventdriven.kafkaapp.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class UserRequest {

    private String name;
    private String lastname;
    private String email;
    private String password;

}
