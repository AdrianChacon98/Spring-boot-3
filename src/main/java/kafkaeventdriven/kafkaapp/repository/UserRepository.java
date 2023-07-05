package kafkaeventdriven.kafkaapp.repository;

import jakarta.transaction.Transactional;
import kafkaeventdriven.kafkaapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {



    @Query(value="SELECT * FROM user u where u.email=:email", nativeQuery = true)
    public Optional<User> findUserByEmail(String email);

    @Query(value = "SELECT * FROM user u WHERE u.verification_code=:token",nativeQuery = true)
    public Optional<User> findByVerificationCode(String token);

    @Modifying
    @Transactional
    @Query(value = "UPDATE User u SET u.enabled=true WHERE u.id=:id", nativeQuery = true)
    public Integer enabledTrue(Integer id);




}
