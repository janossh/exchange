package ua.moyo.rabbitmq.repository;



import ua.moyo.rabbitmq.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
