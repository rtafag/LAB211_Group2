package controller;

import model.User;
import repository.UserRepository;

public class LoginController {

    private final UserRepository userRepo;

    public LoginController(UserRepository repo) {
        this.userRepo = repo;
    }

    public User login(String phoneNumber, String password) {
        User u = userRepo.findByPhoneNumber(phoneNumber);
        if (u != null && u.getPassword().equals(password)) {
            return u;
        }
        throw new IllegalArgumentException("Wrong phone number or password!");
    }
}
