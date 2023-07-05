package kafkaeventdriven.kafkaapp.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kafkaeventdriven.kafkaapp.jwt.JWTUtils;
import kafkaeventdriven.kafkaapp.model.User;
import kafkaeventdriven.kafkaapp.service.UserServiceImp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtFilterAuthorization extends OncePerRequestFilter {

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private UserServiceImp userServiceImp;

    private Logger logger = LoggerFactory.getLogger(JwtFilterAuthorization.class);


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{

            //get the authorization param from headers
            String authorization = request.getHeader("Authorization");


            if(authorization!=null && authorization.startsWith("Bearer")){

                //extract the jwt from authorization param
                String jwt = authorization.substring(7);

                //extract the subject from jwt
                String subject=jwtUtils.getSubject(jwt);

                //verify that the subject is different of null and the context authentication is equal to null
                if(subject!=null && SecurityContextHolder.getContext().getAuthentication()==null) {

                    //get user
                    UserDetails user = userServiceImp.loadUserByUsername(subject);

                    //verify that the user is equal than the jwt
                    if (jwtUtils.isValidateToken(jwt, user)) {

                        //getting all authorities
                        List<GrantedAuthority> authorities = user.getAuthorities().stream().collect(Collectors.toList());

                        User userRole = (User) user;

                        authorities.add(new SimpleGrantedAuthority(userRole.getRole().toString()));

                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user, "", authorities);

                        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                        filterChain.doFilter(request, response);


                    }
                }
            }

            SecurityContextHolder.getContext().setAuthentication(null);
            filterChain.doFilter(request,response);

        }catch (Exception e){
            response.setHeader("Error",e.getMessage());
            response.setStatus(HttpStatus.FORBIDDEN.value());

            logger.info(e.getMessage());

            Map<String,String> error= new HashMap<>();
            error.put("error_message","Jwt expired");
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getOutputStream(),error);
        }
    }



}
