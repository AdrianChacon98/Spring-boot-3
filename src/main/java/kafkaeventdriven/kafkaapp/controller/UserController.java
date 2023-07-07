package kafkaeventdriven.kafkaapp.controller;


import kafkaeventdriven.kafkaapp.model.User;
import kafkaeventdriven.kafkaapp.model.UserRequest;
import kafkaeventdriven.kafkaapp.model.UserResponse;
import kafkaeventdriven.kafkaapp.service.UserServiceImp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static kafkaeventdriven.kafkaapp.exceptionHandler.ExceptionHandlerUnchecked.handlerException;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserServiceImp userServiceImp;

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest user){

        try{

            if(user!=null){

                String response = userServiceImp.signup(user);

                if(response=="The email is not valid")
                    return new ResponseEntity<UserResponse>(new UserResponse("The email is not valid", HttpStatus.BAD_REQUEST.value()),HttpStatus.BAD_REQUEST);
                else if(response=="The user is already exist")
                    return new ResponseEntity<UserResponse>(new UserResponse("The user is already exist",HttpStatus.BAD_REQUEST.value()),HttpStatus.BAD_REQUEST);


            }else{
                return new ResponseEntity<UserResponse>(new UserResponse("The request is empty",HttpStatus.BAD_REQUEST.value()),HttpStatus.BAD_REQUEST);
            }



        }catch (Exception e){
            handlerException(e);
        }

        return new ResponseEntity<UserResponse>(new UserResponse("The user was created successfuly",HttpStatus.CREATED.value()),HttpStatus.CREATED);

    }

    @PostMapping("/signin")
    @ResponseBody
    public ResponseEntity<Map<String,Object>> login(@RequestParam("email") String email, @RequestParam("password") String password)
    {
        Map<String,Object> response = null;

        try{

            response = userServiceImp.login(email,password);

            for(String key : response.keySet()){

                switch (response.get(key).toString()){
                    case "The email it doesnt matching with the rules":
                    case "The user was not found":
                    case "The email or password is not correct please check them and try again":
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    case "Error Login":
                        return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }


        }catch (Exception e){
            handlerException(e);
        }

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/confirm")
    public ResponseEntity<Map<String,Object>> verifyAccount(@RequestParam("token") String token)
    {
        Map<String,Object> response = new HashMap<>();

        try{
            String message = userServiceImp.confirmTokenEmail(token);

            logger.info(token);

            response.put("Message",message);

            if(message.equalsIgnoreCase("The user was not confirmed at time, please create a new user")){
                response.put("HttpStatus",HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            } else if (message.equalsIgnoreCase("the user was not found")) {
                response.put("HttpStatus",HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
            }


        }catch (Exception e){
            handlerException(e);
        }
        response.put("HttpStatus",HttpStatus.OK.value());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }


    @GetMapping("/details/{id}")
    public ResponseEntity<Map<String,Object>> getUser(@PathVariable("id") Integer id)
    {
        try{

            Map<String,Object> response = userServiceImp.getUserId(id);




        }catch (Exception e){

        }
    }




}
