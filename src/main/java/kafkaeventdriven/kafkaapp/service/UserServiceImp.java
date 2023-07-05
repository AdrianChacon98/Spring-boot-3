package kafkaeventdriven.kafkaapp.service;


import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import kafkaeventdriven.kafkaapp.email.EmailSender;
import kafkaeventdriven.kafkaapp.enums.Roles;
import kafkaeventdriven.kafkaapp.jwt.JWTUtils;
import kafkaeventdriven.kafkaapp.model.*;
import kafkaeventdriven.kafkaapp.repository.AuthoritiesRepository;
import kafkaeventdriven.kafkaapp.repository.RoleRepository;
import kafkaeventdriven.kafkaapp.repository.UserRepository;
import kafkaeventdriven.kafkaapp.security.EncodePassword;
import kafkaeventdriven.kafkaapp.service.serviceInterface.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImp implements UserDetailsService, UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthoritiesRepository authoritiesRepository;

    @Autowired
    private RoleRepository roleRepository;


    @Autowired
    private EncodePassword encodePassword;


    @Autowired
    private EmailSender emailSender;

    @Autowired
    private JWTUtils jwtUtils;

    private static Logger logger = LoggerFactory.getLogger(UserServiceImp.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    @Async
    public String signup(UserRequest userRequest)
    {
        try{

            //verify email
            boolean isEmail = isValid(userRequest.getEmail());

            if(isEmail==false)
                //return new UserResponse("The email is not valid", HttpStatus.BAD_REQUEST.value());
                return "The email is not valid";

            //verify if the user doesn't exist
            boolean userExist = userRepository.findUserByEmail(userRequest.getEmail()).isPresent();


            if(userExist){
                //return new UserResponse("The user already exist", HttpStatus.BAD_REQUEST.value());
                return "The user is already exist";
            }else{

                //Change password
                String hashPassword = generatePassword(userRequest.getPassword());

                userRequest.setPassword(hashPassword);
                UUID uuid = UUID.randomUUID();

                //creating a new user

                User user = new User();
                user.setName(userRequest.getName());
                user.setLastname(userRequest.getLastname());
                user.setEmail(userRequest.getEmail());
                user.setPassword(userRequest.getPassword());

                user.setCreateAt(LocalDateTime.now());
                user.setExpiredAt(LocalDateTime.now().plusMinutes(15));
                user.setVerificationCode(uuid.toString());
                user.setEnabled(false);
                user.setLocked(true);


                //Creating a new Role
                Role role = new Role();
                role.setRoleName(Roles.ROLE_USER.name());
                role.setUserRole(user);

                user.setRole(role);



                //Creating authorities for the user
                List<Authorities> authorities = new ArrayList<>();

                Roles.ROLE_USER.getAuthorities().stream().forEach(authority->{
                    authorities.add(new Authorities(authority.name(),user));
                });



                user.setAuthorities(authorities);

                //First save user because other objects are going to reference that user if it doesnt exist it will throw an exception
                userRepository.save(user);
                roleRepository.save(role);
                authoritiesRepository.saveAll(authorities);

                //Creating the hash and sending the email for activate the new user account


                String endpoint="http://localhost:8000/api/v1/user/confirm?token="+uuid;


                emailSender.send(user.getEmail(),buildEmail(user.getUsername(),endpoint));

                return "The user was created successfuly";


            }


        }catch (Exception e){

            logger.info(e.getMessage()+e.getCause());

            return "Error internal server";

        }


    }


    @Override
    public Map<String, Object> login(String email, String password){

        Map<String, Object> tokens=null;

        try{

            boolean isEmail= isValid(email);

            if(!isEmail){
                Map<String,Object> errorEmail=new HashMap<>();
                errorEmail.put("ErrorEmail","The email it doesnt matching with the rules");
                return errorEmail;
            }

            Optional<User> user = userRepository.findUserByEmail(email);

            if(user.isEmpty()){
                Map<String, Object> userNotFound = new HashMap<>();

                userNotFound.put("UserNotFound","The user was not found");

                return  userNotFound;
            }


            if(user.get().getEmail().equals(email) && isPasswordEquals(user.get().getPassword(),password)){
                tokens=jwtUtils.generateJWT(user.get());
            }else{
                Map<String,Object> userIsNotCorrect = new HashMap<>();
                userIsNotCorrect.put("UserOrPasswordIsNotcorrect","The email or password is not correct please check them and try again");
                return  userIsNotCorrect;
            }


        }catch(Exception e){
            Map<String,Object> serverError = new HashMap<>();
            logger.info(e.getMessage().toString());
            serverError.put("Server error","Error Login");

            return serverError;
        }

        return tokens;
    }




    @Override
    public String confirmTokenEmail(String token){
        try{

            Optional<User> user = userRepository.findByVerificationCode(token);



            if(!user.isEmpty()){

                if(user.get().getExpiredAt().isBefore(LocalDateTime.now())){
                    return "The user was not confirmed at time, please create a new user";
                }else{


                    userRepository.enabledTrue(user.get().getId());

                }

            }else{
                return "the user was not found";
            }

        }catch(Exception e)
        {

        }
        return "The user was confirmed at time";

    }

    @Override
    public Map<String,Object> refreshToken(String access, String refresh){
        Map<String, Object> tokens=null;

        try{

            boolean isValid=jwtUtils.isValidateRefreshToken(access,refresh);

            if(isValid){
                Claims claims = jwtUtils.getBodyClaims(access);
                tokens = jwtUtils.refresToken(claims,refresh);
            }else{
                tokens.put("Error","refresh_token is expired login again");

            }

        }catch (Exception e){
            logger.info(e.getMessage());
            tokens.put("Server error","Internal server error");
            return tokens;
        }
        return tokens;
    }




    public boolean isValid(String email){


        Pattern pattern = Pattern.compile("^[_a-z0-9-]+(.[_a-z0-9-]+)*@[a-z0-9-]+(.[a-z0-9-]+)*(.[a-z]{2,4})$");

        Matcher matcher = pattern.matcher(email);

        if(matcher.find())
            return true;
        else{
            return false;
        }

    }

    public String generatePassword(String password){

        int saltLength = 16; // salt length in bytes
        int hashLength = 32; // hash length in bytes
        int parallelism = 1; // currently is not supported
        int memory = 4096; // memory costs
        int iterations = 3;

        Argon2PasswordEncoder passwordEncoder = new Argon2PasswordEncoder(saltLength,hashLength,parallelism,memory,iterations);

        return passwordEncoder.encode(password);
    }

    public boolean isPasswordEquals(String userPassword,String password){
        int saltLength = 16; // salt length in bytes
        int hashLength = 32; // hash length in bytes
        int parallelism = 1; // currently is not supported
        int memory = 4096; // memory costs
        int iterations = 3;

        Argon2PasswordEncoder passwordEncoder = new Argon2PasswordEncoder(saltLength,hashLength,parallelism,memory,iterations);

        return passwordEncoder.matches(password,userPassword);

    }

    /*
    //these can be move it to other class
    private String generateHash(String password){

        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2d);

        return argon2.hash(5,1024*1,2, password);


    }

    private boolean isPasswordEquals(String userPassword,String password){
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2d);
        return argon2.verify(userPassword,password);
    }*/


    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }


}
