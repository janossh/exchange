package ua.moyo.rabbitmq.repository;



import ua.moyo.rabbitmq.model.Logger;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Logger, Long> {

}
