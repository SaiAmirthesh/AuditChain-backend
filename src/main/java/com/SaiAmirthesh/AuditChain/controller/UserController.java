package com.SaiAmirthesh.AuditChain.controller;

import com.SaiAmirthesh.AuditChain.entity.Account;
import com.SaiAmirthesh.AuditChain.entity.Transaction;
import com.SaiAmirthesh.AuditChain.repository.AccountRepository;
import com.SaiAmirthesh.AuditChain.repository.TransactionRepository;
import com.SaiAmirthesh.AuditChain.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/dashboard")
    public Account dashboard() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountRepository.findByAccountNumber(username); // Using username as account number placeholder, adjust based on actual design
    }

    @GetMapping("/transactions")
    public List<Transaction> transactions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return transactionRepository.findUserTransactions(username);
    }

    @PostMapping("/transfer")
    public String transfer(@org.springframework.web.bind.annotation.RequestBody com.SaiAmirthesh.AuditChain.dto.TransferRequest request) {

        return accountService.transfer(
            request.getFromAccount(), 
            request.getToAccount(), 
            request.getAmount()
        );
    }
}
