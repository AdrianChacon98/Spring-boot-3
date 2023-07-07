package kafkaeventdriven.kafkaapp;


import kafkaeventdriven.kafkaapp.enums.Roles;
import kafkaeventdriven.kafkaapp.model.Authorities;
import kafkaeventdriven.kafkaapp.model.Role;
import kafkaeventdriven.kafkaapp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@SpringBootApplication
public class KafkaAppApplication implements CommandLineRunner {

	private Logger logger = LoggerFactory.getLogger(KafkaAppApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(KafkaAppApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		User user = new User();

		//creating a new user
		user.setName("Adrian");
		user.setLastname("Chacon");
		user.setEmail("adrian.chacon22@hotmail.com");
		user.setPassword("password");

		user.setCreateAt(LocalDateTime.now());
		user.setExpiredAt(LocalDateTime.now().plusMinutes(15));
		user.setVerificationCode("codigo");
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

		logger.info(user.getAuthorities().toString());

		List<GrantedAuthority> grantedAuthorityList = user.getAuthorities()
				.stream()
				.map(grantedAuthority -> new SimpleGrantedAuthority(grantedAuthority.getAuthority()))
				.peek(System.out::println)
				.collect(Collectors.toList());
	}
}
