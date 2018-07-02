package ua.moyo.rabbitmq.repository;

import ua.moyo.rabbitmq.model.Database;

import org.springframework.data.jpa.repository.JpaRepository;


public interface DatabaseRepository extends JpaRepository<Database, Long> {
}
