package com.vbs.demo.controller;

import com.vbs.demo.dto.TransactionDto;
import com.vbs.demo.dto.TransferDto;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.pulsar.PulsarProperties;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    TransactionRepo transactionRepo;
    @Autowired
    UserRepo userRepo;

    @PostMapping("/deposit")
    public String deposit(@RequestBody TransactionDto obj){
        User user=userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("Wrong Id"));
        user.setBalance(user.getBalance()+obj.getAmount());
        userRepo.save(user);

        Transaction t=new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(user.getBalance());
        t.setDescription("Rs "+obj.getAmount()+" Deposit Successful");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return "Deposit Successful";

    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestBody TransactionDto obj){
        User user=userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("Wrong Id"));
        if(obj.getAmount()>user.getBalance()) {
            return "Insufficient balance";
        }
        user.setBalance(user.getBalance() - obj.getAmount());
        userRepo.save(user);

        Transaction t=new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(user.getBalance());
        t.setDescription("Rs "+obj.getAmount()+" Withdrawal Successful");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return "Withdrawal Successful";

    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferDto obj){
        User sender=userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("Not found"));
        User receiver=userRepo.findByUsername(obj.getUsername());
        if(receiver==null) return "Receiver Not found";
        if(sender.getId()== receiver.getId()) return "Self transaction not allowed";
        if(obj.getAmount()<=0) return "Invalid Amount";
        if(obj.getAmount()> sender.getBalance()) return "Insufficient Balance";
        sender.setBalance(sender.getBalance()- obj.getAmount());
        receiver.setBalance(receiver.getBalance()+ obj.getAmount());
        userRepo.save(sender);
        userRepo.save(receiver);

        Transaction t1=new Transaction();
        Transaction t2=new Transaction();

        t1.setAmount(obj.getAmount());
        t1.setCurrBalance(sender.getBalance());
        t1.setDescription("Rs "+obj.getAmount()+" Sent to user "+receiver.getUsername());
        t1.setUserId(sender.getId());
        transactionRepo.save(t1);

        t2.setAmount(obj.getAmount());
        t2.setCurrBalance(receiver.getBalance());
        t2.setDescription("Rs "+obj.getAmount()+" Received from user "+sender.getUsername());
        t2.setUserId(receiver.getId());
        transactionRepo.save(t2);

        return "Transfer Done Successfully";
    }

    @GetMapping("/passbook/{id}")
    public List<Transaction> getPassbook(@PathVariable int id){
        return transactionRepo.findAllByUserId(id);
    }
}
