package kafkaeventdriven.kafkaapp.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.xml.bind.DatatypeConverter;
import kafkaeventdriven.kafkaapp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static kafkaeventdriven.kafkaapp.exceptionHandler.ExceptionHandlerUnchecked.handlerException;

@Component
public class JWTUtils {


    private final Logger logger = LoggerFactory.getLogger(JWTUtils.class);


    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.time-expires}")
    private String timeMS;

    private String access_token;

    private String refresh_token;

    private Map<String,Object> tokens;




    public Map<String,Object> generateJWT(User user){

        tokens = new HashMap<>();


        try{

            //Algorithm hash
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

            //Time token
            long nowMillis=System.currentTimeMillis();
            Date now = new Date(nowMillis);

            //get the bites from the secret key parse as a  binary method
            byte [] secretKeyBites = DatatypeConverter.parseBase64Binary(secretKey);

            //create the signingKey to sign the token
            Key signing = new SecretKeySpec(secretKeyBites,signatureAlgorithm.getJcaName());

            //Get authorities
            List<GrantedAuthority> grantedAuthorityList = user.getAuthorities()
                    .stream()
                    .map(grantedAuthority -> new SimpleGrantedAuthority(grantedAuthority.getAuthority()))
                    .collect(Collectors.toList());



            //create claims
            Map<String,Object> claims = new HashMap<>();


            //get roles


            //Set claims values
            claims.put("username",user.getUsername());
            claims.put("Role",user.getRole());
            claims.put("Authorities",grantedAuthorityList.stream().map(authority->authority.getAuthority()).collect(Collectors.toList()));

            //create jwt

            if(!timeMS.isEmpty()){

                //Set time expiration to the token
                long expireMs = nowMillis + Long.parseLong(timeMS);
                Date expireAt = new Date(expireMs);

                access_token = Jwts.builder()
                        .setIssuedAt(new Date(System.currentTimeMillis()))
                        .setClaims(claims)
                        .setSubject(user.getEmail())
                        .setExpiration(expireAt)
                        .signWith(signing,signatureAlgorithm).compact();


                refresh_token = Jwts.builder()
                        .setIssuedAt(new Date(System.currentTimeMillis()))
                        .setSubject(user.getEmail())
                        .setExpiration(new Date(System.currentTimeMillis() + (Long.parseLong((timeMS)) * 2) * 24))//set 1 day
                        .signWith(signing, signatureAlgorithm).compact();

            }




        }catch (Exception e){

            handlerException(e);

        }


        logger.info(access_token);
        logger.info(refresh_token);

        tokens.put("access_token",access_token);
        tokens.put("refresh_token",refresh_token);


        return tokens;

    }

    public Map<String,Object> refresToken(Claims claims,String  subject){

        access_token="";
        refresh_token="";
        tokens=new HashMap<>();

        try{

            //Algorithm hash
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

            //Time token
            long nowMillis=System.currentTimeMillis();
            Date now = new Date(nowMillis);

            //get the bites from the secret key parse as a  binary method
            byte [] secretKeyBites= DatatypeConverter.parseBase64Binary(secretKey);


            //create the signingKey to sign the token
            Key signing = new SecretKeySpec(secretKeyBites,signatureAlgorithm.getJcaName());


            //set time expiration to the token
            long expireMs=nowMillis+Long.parseLong(timeMS);
            Date expirationAt = new Date(expireMs);

            //create jwt
            access_token = String.valueOf(Jwts.builder()
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setSubject(subject)
                    .setClaims(claims)
                    .setExpiration(expirationAt)
                    .signWith(signing,signatureAlgorithm));

            //create refresh_token
            refresh_token= String.valueOf(Jwts.builder()
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setSubject(claims.getSubject())
                    .setExpiration(new Date(System.currentTimeMillis()+(Long.parseLong((timeMS))*2)*24))//set 1 day
                    .signWith(signing,signatureAlgorithm));



        }catch (Exception e){
            handlerException(e);
        }

        logger.info(access_token);
        logger.info(refresh_token);

        tokens.put("access_token",access_token);
        tokens.put("refresh_token",refresh_token);

        return tokens;

    }


    public Claims getBodyClaims(String token){
        return (Claims) Jwts.parserBuilder().setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
                .build().parse(token).getBody();
    }

    public String getSubject(String token){
        Claims claims = getBodyClaims(token);

        return claims.getSubject();
    }

    public Long getId(String token){
        Claims claims = getBodyClaims(token);
        return Long.parseLong(claims.getId());
    }

    public boolean isTokenExpired(String token){
        Claims claims = getBodyClaims(token);
        return claims.getExpiration().before(new Date());
    }

    public boolean isValidateToken(String token, UserDetails user)
    {
        return user.getUsername().equals(getBodyClaims(token).get("username")) && !isTokenExpired(token);
    }

    public boolean isValidateRefreshToken(String access_token, String refresh_token){
        return getSubject(refresh_token).equals(getSubject(access_token)) && !isTokenExpired(refresh_token);
    }
}
