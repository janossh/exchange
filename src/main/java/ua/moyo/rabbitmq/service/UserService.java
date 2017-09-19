package ua.moyo.rabbitmq.service;

import ua.moyo.rabbitmq.model.User;

/**
 * Created by JLD on 27.05.2017.
 */
public interface UserService {

    User getUserByEmail(String email);
    boolean isUserExistFindByEmail(String email);

}
