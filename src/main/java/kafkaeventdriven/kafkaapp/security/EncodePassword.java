package kafkaeventdriven.kafkaapp.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class EncodePassword {

    @Bean
    public PasswordEncoder passwordEncoder() {

        int saltLength = 16; // salt length in bytes
        int hashLength = 32; // hash length in bytes
        int parallelism = 1; // currently is not supported
        int memory = 4096; // memory costs
        int iterations = 3;

        return new Argon2PasswordEncoder(saltLength,hashLength, parallelism,memory,iterations);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder()
    {
        return new BCryptPasswordEncoder(10);
    }
}
