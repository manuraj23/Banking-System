package com.bank.bank.Service;

import com.bank.bank.Entity.Amount;
import com.bank.bank.Entity.Pay;
import com.bank.bank.Entity.Statement;
import com.bank.bank.Entity.User;
import com.bank.bank.Repository.StatementRepository;
import com.bank.bank.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class UserService {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public StatementRepository statementRepository;



    private static final PasswordEncoder passwordEncoder= new BCryptPasswordEncoder();
    public void saveNewUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(List.of("USER"));
        user.setBalance(0f);
        userRepository.save(user);
    }

    public User findByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public void saveUser(User user){
        userRepository.save(user);

    }
    public void saveStatement(String username, Statement statement) {
        try{
            User user = findByUsername(username);
            Statement save= statementRepository.save(statement);
            user.getStatement().add(save);
            saveUser(user);
        } catch (Exception e) {
            throw new RuntimeException("Error saving bank entry", e);
        }
    }

    public void pay(String username, Pay pay) {
        User payer = findByUsername(username);
        String receiver_name = pay.getTo();
        User receiver = findByUsername(receiver_name);
        if(receiver == null){
            throw new RuntimeException("user not found");
        }
        else{
            Float amount=pay.getAmt();
            Float balance=payer.getBalance();
            Float recieverBalance=receiver.getBalance();
            Float newBalance=balance-amount;
            receiver.setBalance(recieverBalance+amount);
            payer.setBalance(newBalance);
            userRepository.save(payer);

            userRepository.save(receiver);

            Float payerNewBalance = newBalance;
            Float receiverNewBalance = receiver.getBalance() + amount;
            Statement payerStatement = new Statement();
            payerStatement.setKind("Debit");
            payerStatement.setAmt(amount);
            payerStatement.setUpdated_bal(payerNewBalance);
            payerStatement.setDate(LocalDateTime.now());
            statementRepository.save(payerStatement);


            Statement receiverStatement = new Statement();
            receiverStatement.setKind("Credit");
            receiverStatement.setAmt(amount);
            receiverStatement.setUpdated_bal(receiverNewBalance);
            receiverStatement.setDate(LocalDateTime.now());
            statementRepository.save(receiverStatement);

            // Add statement references to users
            payer.getStatement().add(payerStatement);
            receiver.getStatement().add(receiverStatement);

            // Save updated users
            userRepository.save(payer);
            userRepository.save(receiver);
        }
    }


    public Void addMoney(String username, Amount amount) {
        User user = findByUsername(username);
        Float currentBalance = user.getBalance();
        Float toAdd = amount.getAmt();
        Float newBalance = currentBalance + toAdd;

        // Update user balance
        user.setBalance(newBalance);

        // Create and save statement
        Statement statement = new Statement();
        statement.setKind("Credit");
        statement.setAmt(toAdd);
        statement.setUpdated_bal(newBalance);
        statement.setDate(LocalDateTime.now());
        statementRepository.save(statement);

        // Add statement reference to user
        user.getStatement().add(statement);

        // Save user
        userRepository.save(user);

        return null;
    }


}
