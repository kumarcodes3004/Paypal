package com.paypal.user_service.controller;


import com.paypal.user_service.entity.User;
import com.paypal.user_service.service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@Slf4j
@RestController
@RequestMapping("/api/user-service")
public class UserController {

    private UserServiceImpl userService;


    UserController (UserServiceImpl userService){
       this.userService = userService;
    }

    @PostMapping("user")
    public ResponseEntity<User> createUser(@RequestBody User user){

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){

        return userService.getUserById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(){
        log.info("Inside get users controller");

        return  ResponseEntity.ok(userService.getAllUsers());
    }
}
