package cse308.map.controller;

import cse308.map.model.User;
import cse308.map.server.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/homepage")
@CrossOrigin
public class AdminController {
    @Autowired
    private UserService userService;

    @PostMapping(value = "/admin")//check the user exist in the database or not
    public ResponseEntity<?> findAllUsers() {
        Iterable<User> users = userService.findAll();
        List<User> userList  = new ArrayList<>();
        for(User user : users){
            userList.add(user);
        }
        return new ResponseEntity<List<User>>(userList, HttpStatus.OK);
    }

    @PostMapping(value = "/delete")//check the user exist in the database or not
    public ResponseEntity<?> deletedUsers(@RequestBody User user) {
        userService.isValidUser(user);
        userService.deleted(user.getEmail());
        return new ResponseEntity<>("finish deleted user", HttpStatus.OK);
    }

    @PostMapping(value = "/update")//check the user exist in the database or not
    public ResponseEntity<?> updatedUsers(@RequestBody User user) {
        userService.isValidUser(user);
        userService.updateUser(userService.registerUser(user));
        return new ResponseEntity<>("finish update user", HttpStatus.OK);
    }

    @PostMapping(value = "/register")//check the user exist in the database or not
    public ResponseEntity<?> registerdUsers(@RequestBody User user) {
        if(userService.isValidUser(user))
            return new ResponseEntity<>("user already exist", HttpStatus.OK);
        userService.registerUser(user);
        return new ResponseEntity<>("register successful", HttpStatus.OK);
    }

}
