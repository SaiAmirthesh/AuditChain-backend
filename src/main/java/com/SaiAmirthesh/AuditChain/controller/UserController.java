package com.SaiAmirthesh.AuditChain.controller;

import com.SaiAmirthesh.AuditChain.entity.Account;
import com.SaiAmirthesh.AuditChain.entity.Transaction;
import com.SaiAmirthesh.AuditChain.repository.AccountRepository;
import com.SaiAmirthesh.AuditChain.repository.TransactionRepository;
import com.SaiAmirthesh.AuditChain.service.AccountService;
import com.SaiAmirthesh.AuditChain.service.AnalyticsService;
import com.SaiAmirthesh.AuditChain.service.AIAgentService;
import com.SaiAmirthesh.AuditChain.dto.UserAnalyticsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private AIAgentService aiAgentService;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByAccountNumber(username);
        UserAnalyticsDTO analytics = analyticsService.getUserAnalytics(username);
        
        Map<String, Object> response = new HashMap<>();
        if (account != null) {
            response.put("account", account); // Keep for UserDashboard.jsx (expects account.account.balance)
            response.put("accountNumber", account.getAccountNumber()); // Add for UserTransfer.jsx (expects account.accountNumber)
            response.put("balance", account.getBalance());
            response.put("holderName", account.getHolderName());
            response.put("id", account.getId());
        }
        
        response.put("totalIncome", analytics.getTotalIncome());
        response.put("totalExpense", analytics.getTotalExpense());
        return response;
    }

    @GetMapping("/transactions")
    public Page<Transaction> transactions(@RequestParam(defaultValue = "0") int page, 
                                        @RequestParam(defaultValue = "10") int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findByFromAccountOrToAccountOrderByTimestampDesc(username, username, pageable);
    }

    @PostMapping("/transfer")
    public String transfer(@org.springframework.web.bind.annotation.RequestBody com.SaiAmirthesh.AuditChain.dto.TransferRequest request) {

        return accountService.transfer(
            request.getFromAccount(), 
            request.getToAccount(), 
            request.getAmount()
        );
    }

    @GetMapping("/ai-insights")
    public String getAiInsights() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAnalyticsDTO data = analyticsService.getUserAnalytics(username);
        return aiAgentService.getUserInsights(data);
    }

    @GetMapping("/analytics")
    public UserAnalyticsDTO getAnalytics() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return analyticsService.getUserAnalytics(username);
    }
}
