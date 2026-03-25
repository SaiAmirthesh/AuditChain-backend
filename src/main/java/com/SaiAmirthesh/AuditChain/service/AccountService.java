package com.SaiAmirthesh.AuditChain.service;

import com.SaiAmirthesh.AuditChain.entity.Account;
import com.SaiAmirthesh.AuditChain.entity.Transaction;
import com.SaiAmirthesh.AuditChain.repository.AccountRepository;
import com.SaiAmirthesh.AuditChain.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccountService {
    
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;


    @Transactional
    public String transfer(String fromAcc, String toAcc, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid transfer amount");
        }

        Account from = accountRepository.findByAccountNumber(fromAcc);
        Account to = accountRepository.findByAccountNumber(toAcc);

        if (from == null || to == null) {
            throw new IllegalArgumentException("One or both accounts do not exist");
        }

        if (from.getBalance() < amount) {
            throw new IllegalStateException("Insufficient balance");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        from.setBalance(from.getBalance() - amount);
        from.setUpdatedBy(username);

        to.setBalance(to.getBalance() + amount);
        to.setUpdatedBy(username);

        accountRepository.save(from);
        accountRepository.save(to); 



        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAcc);
        transaction.setToAccount(toAcc);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        return "transfer successful";
    }
    
    public java.util.List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}
