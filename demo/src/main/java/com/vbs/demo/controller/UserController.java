package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    HistoryRepo histroyRepo;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        User usersameusername = userRepo.findByUsername(user.getUsername());
        User usersameemail = userRepo.findByEmail(user.getEmail());
        if (usersameusername != null) return "Username already exists";
        if (usersameemail != null) return "Email already exists";
        History h1=new History();
        h1.setDescription("User Self Created "+user.getUsername());
        histroyRepo.save(h1);
        userRepo.save(user);
        return "Signup Successful";

    }


    @PostMapping("/login")
    public String login(@RequestBody LoginDto u) {
        User user = userRepo.findByUsername(u.getUsername());
        if (user == null) return "User not found";
        if (!u.getPassword().equals(user.getPassword())) return "Password incorrect";
        if (!u.getRole().equals(user.getRole())) return "Role incorrect";
        return String.valueOf(user.getId());
    }

    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id) {
        User user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        DisplayDto displayDto = new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());
        return displayDto;
    }

    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj) {
        User user = userRepo.findById(obj.getId()).orElseThrow(() -> new RuntimeException("User not found"));
        History h1=new History();
        if ((obj.getKey().equalsIgnoreCase("name")) && (!obj.getValue().equalsIgnoreCase(user.getName()))) {
            user.setName(obj.getValue());
            h1.setDescription("User change Name from "+user.getName()+" to "+obj.getValue());
            histroyRepo.save(h1);
            userRepo.save(user);
            return "Update done successfully";
        } else if ((obj.getKey().equalsIgnoreCase("password")) && (!obj.getValue().equalsIgnoreCase(user.getName()))) {
            user.setPassword(obj.getValue());
            userRepo.save(user);
            h1.setDescription("User change Password "+user.getUsername() );
            histroyRepo.save(h1);

            return "Update done successfully";
        } else if ((obj.getKey().equalsIgnoreCase("email")) && (!obj.getValue().equalsIgnoreCase(user.getName()))) {
            User usertest = userRepo.findByEmail(obj.getValue());
            if (usertest != null) return "Email already exists";
            user.setEmail(obj.getValue());
            userRepo.save(user);
            h1.setDescription("User change Email from "+user.getEmail()+" to "+obj.getValue());
            histroyRepo.save(h1);

            return "Update done successfully";
        } else {
            return "Invalid";
        }


    }

    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user,@PathVariable int adminId) {
        User usersameusername = userRepo.findByUsername(user.getUsername());
        User usersameemail = userRepo.findByEmail(user.getEmail());
        if (usersameusername != null) return "Username already exists";
        if (usersameemail != null) return "Email already exists";
        History h1=new History();
        h1.setDescription("User "+user.getUsername()+" Created By Admin "+adminId);
        histroyRepo.save(h1);
        userRepo.save(user);
        return "Successfully added";

    }

    @GetMapping("/users")
    public List<User> getallusers(@RequestParam String sortBy, @RequestParam String order) {
        Sort sort;
        if (order.equalsIgnoreCase("desc")) {
            sort = Sort.by(sortBy).descending();
        } else {
            sort = Sort.by(sortBy).ascending();
        }
        return userRepo.findAllByRole("customer", sort);
    }

    @GetMapping("users/{keyword}")
    public List<User> getUser(@PathVariable String keyword) {
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword, "customer");

    }

    @DeleteMapping("/delete-user/{userId}/admin/{adminId}")
    public String deleteUser(@PathVariable int userId,@PathVariable int adminId){
        User user=userRepo.findById(userId).orElseThrow(()->new RuntimeException("User Not found"));
        if(user.getBalance()>0) return "Balance should be zero";
        History h1=new History();
        h1.setDescription("User "+user.getUsername()+" Deleted By Admin "+adminId);
        histroyRepo.save(h1);
        userRepo.delete(user);
        return "user Deleted Successfully";
    }
}