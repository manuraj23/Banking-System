package com.bank.bank.Service;

import com.bank.bank.Entity.User;
import com.bank.bank.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Component
public class AdminService {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/all-users")
    public List<User> getAll() {
        return userRepository.findAll();
    }
}
