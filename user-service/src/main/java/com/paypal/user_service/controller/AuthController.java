package com.paypal.user_service.controller;

import com.paypal.user_service.dto.JwtResponse;
import com.paypal.user_service.dto.LoginRequest;
import com.paypal.user_service.dto.SignUpRequest;
import com.paypal.user_service.entity.User;
import com.paypal.user_service.repository.UserRepository;
import com.paypal.user_service.utils.JWTUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    private final JWTUtils jwtUtils;

    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,JWTUtils jwtUtils, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.jwtUtils=jwtUtils;
        this.passwordEncoder =passwordEncoder;
    }


    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest request){
        Optional<User> existingUser =userRepository.findByEmail(request.getEmail());
        if(existingUser.isPresent()){
            return ResponseEntity.badRequest().body("User already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setRole("ROLE_USER");
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return  ResponseEntity.ok("User Registered Successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?>  login(@RequestBody LoginRequest request){

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if(userOpt.isEmpty()){
            return  ResponseEntity.status(401).body("User not found");
        }

        User user =userOpt.get();

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            return ResponseEntity.status(401).body("Invalid Credentials");
        }

        //Add role tp claims

        Map<String , Object> claims = new HashMap<>();
        claims.put("role",user.getRole());

        //generate token with claims
        //its same how forgerock sends a Jwt token on successfull auth.
        String token = jwtUtils.generateToken(claims, user.getEmail());

        return ResponseEntity.ok(new JwtResponse(token));
    }

}
