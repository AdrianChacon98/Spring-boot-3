package kafkaeventdriven.kafkaapp.service.serviceInterface;

import kafkaeventdriven.kafkaapp.model.UserRequest;
import kafkaeventdriven.kafkaapp.model.UserResponse;

import java.util.Map;

public interface UserService {


    public String signup(UserRequest userRequest);
    public String confirmTokenEmail(String token);

    public Map<String,Object> login(String email, String password);

    public Map<String,Object> refreshToken(String access, String refresh);

}
