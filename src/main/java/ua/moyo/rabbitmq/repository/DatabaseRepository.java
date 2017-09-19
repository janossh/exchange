package ua.moyo.rabbitmq.repository;

import ua.moyo.rabbitmq.model.Database;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by JLD on 31.07.2017.
 */
public interface DatabaseRepository extends JpaRepository<Database, Long> {
}
