package cse308.map.server;


import cse308.map.model.User;
import cse308.map.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

    public Iterable<User> findAll(){
        return userRepository.findAll();
    }

    public Optional<User> findById(String email){
        return  userRepository.findById(email);
    }
    public void deleted(String email){
        userRepository.deleteById(email);
    }

    public Boolean isValidUser(User user) {
        Optional<User> opt = userRepository.findById(user.getEmail());
        if (!opt.isPresent()) {//check if the account exist
            return false;
        } else {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            if (passwordEncoder.matches(user.getPassword(),opt.get().getPassword())) {//check if the password is corrected.
                return true;
            }
            else
            return false;
        }
    }
    public void updateUser(User user){
        userRepository.save(user);
    }


}
