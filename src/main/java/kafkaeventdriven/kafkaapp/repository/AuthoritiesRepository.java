package kafkaeventdriven.kafkaapp.repository;

import kafkaeventdriven.kafkaapp.model.Authorities;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthoritiesRepository extends JpaRepository<Authorities,Integer> {

}
