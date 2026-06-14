package com.paypal.user_service.service;

import com.paypal.user_service.client.WalletClient;
import com.paypal.user_service.dto.CreateWalletRequest;
import com.paypal.user_service.entity.User;
import com.paypal.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletClient walletClient;

    public UserServiceImpl(UserRepository userRepository, WalletClient walletClient) {
        this.userRepository = userRepository;
        this.walletClient = walletClient;
    }

    @Override
    public User createUser(User user) {

        User savedUser = userRepository.save(user);

        try{
            CreateWalletRequest request = new CreateWalletRequest();
            request.setUserId(user.getId());
            request.setCurrency("INR");
            walletClient.createWallet(request);
        }catch (Exception e){
            //rollback
            userRepository.deleteById(user.getId());
            throw new RuntimeException("Wallet creation failed,user rolled back",e);
        }

        return savedUser;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Inside getAllUsers mtd at service level");
        return userRepository.findAll();
    }
}
