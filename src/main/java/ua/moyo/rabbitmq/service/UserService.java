package ua.moyo.rabbitmq.service;

import ua.moyo.rabbitmq.model.User;


public interface UserService {

    User getUserByEmail(String email);
    boolean isUserExistFindByEmail(String email);

}
