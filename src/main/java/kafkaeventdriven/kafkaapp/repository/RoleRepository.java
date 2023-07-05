package kafkaeventdriven.kafkaapp.repository;

import kafkaeventdriven.kafkaapp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role,Integer> {
}
