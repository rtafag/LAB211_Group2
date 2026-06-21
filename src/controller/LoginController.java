package controller;

import model.User;
import repository.UserRepository;

public class LoginController {
    private final UserRepository userRepo;

    public LoginController(UserRepository repo) {
        this.userRepo = repo;
    }

    public User login(String id, String password) {
        User u = userRepo.findById(id);
        if (u != null && u.getPassword().equals(password)) {
            return u;
        }
        throw new IllegalArgumentException("Sai ID hoặc mật khẩu!");
    }
}
