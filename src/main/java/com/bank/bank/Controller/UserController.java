package com.bank.bank.Controller;

import com.bank.bank.Converter.ExchangeRateResponse;
import com.bank.bank.Entity.Amount;
import com.bank.bank.Entity.Pay;
import com.bank.bank.Entity.Statement;
import com.bank.bank.Entity.User;
import com.bank.bank.Repository.StatementRepository;
import com.bank.bank.Repository.UserRepository;
import com.bank.bank.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    StatementRepository statementRepository;

    @GetMapping("/checkBalance")
    public ResponseEntity<?> checkBalance() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Float balance = userRepository.findByUsername(authentication.getName()).getBalance();
        return ResponseEntity.ok("balance: " + balance);
    }

    @PostMapping("/fund")
    public ResponseEntity<?>fundUser(@RequestBody Amount amount ){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            float amountToAdd = amount.getAmt();
            if (amountToAdd <= 0) {
                return ResponseEntity.badRequest().body("Amount must be positive");
            }
            userService.addMoney(username,amount);

            return ResponseEntity.ok("Amount added successfully. ");

        }catch (Exception e){
            return new ResponseEntity<>(amount, HttpStatus.BAD_REQUEST);
        }


    }

    @GetMapping("/stmt")
    public ResponseEntity<?> getStatement(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        List<Statement> statements = user.getStatement();
        if(statements!=null && !statements.isEmpty()){
            return new ResponseEntity<>(statements, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestBody Pay pay){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Float balance = userRepository.findByUsername(username).getBalance();
        Float pay_bal= pay.getAmt();
        if(pay_bal>balance){
            return ResponseEntity.badRequest().body("Low Amount. Current balance: " + balance);
        }
        String receiver= pay.getTo();
        User payment_reciever = userRepository.findByUsername(receiver);
        if(payment_reciever==null){
            return ResponseEntity.badRequest().body("Receiver not found");
        }
        userService.pay(username,pay);
        return ResponseEntity.ok("Payment successful");
    }

    @GetMapping("/bal")
    public ResponseEntity<?> getBalanceInCurrency(@RequestParam(defaultValue = "USD") String currency) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        Float inrBalance = user.getBalance();

        String apiUrl = "https://open.er-api.com/v6/latest/USD";
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<ExchangeRateResponse> response =
                    restTemplate.getForEntity(apiUrl, ExchangeRateResponse.class);
            ExchangeRateResponse exchangeData = response.getBody();

            if (exchangeData != null && "success".equalsIgnoreCase(exchangeData.getResult())) {
                Map<String, Float> rates = exchangeData.getRates();

                if (!rates.containsKey("INR") || !rates.containsKey(currency)) {
                    return ResponseEntity.badRequest().body("Unsupported currency or INR not found in rates.");
                }

                // Convert INR to USD base, then to target currency
                Float rateINR = rates.get("INR");
                Float rateTarget = rates.get(currency);

                Float usdValue = inrBalance / rateINR;
                Float converted = usdValue * rateTarget;

                Map<String, Object> result = new HashMap<>();

                result.put("original_currency", "INR");
                result.put("balance_in_inr", inrBalance);
                result.put("converted_currency", currency);
                result.put("converted_balance", converted);
                result.put("rate_used", rateTarget / rateINR);  // effective rate from INR to target

                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Failed to fetch exchange rates.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }



}
