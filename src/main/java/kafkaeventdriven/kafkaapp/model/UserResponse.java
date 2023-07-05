package kafkaeventdriven.kafkaapp.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class UserResponse {

    private String message;

    private int status;

}
