package ua.moyo.rabbitmq.service.ServiceImpl;

import ua.moyo.rabbitmq.model.User;
import ua.moyo.rabbitmq.repository.UserRepository;
import ua.moyo.rabbitmq.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    @Override
    public User getUserByEmail(String email) {

        User jld = userRepository.findOne(31L);

        Query query = entityManager.createQuery("select user from User user where user.email = :email", User.class);
        query.setParameter("email",email);
        List<User> userList = query.getResultList();

        if(userList.isEmpty()){return new User();}
        else{return userList.get(0);}

    }

    @Override
    public boolean isUserExistFindByEmail(String email) {
        return getUserByEmail(email).getId()!=null;

    }

}
