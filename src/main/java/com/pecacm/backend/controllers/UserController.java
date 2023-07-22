package com.pecacm.backend.controllers;

import com.pecacm.backend.entities.User;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.model.AssignRoleRequest;
import com.pecacm.backend.model.AuthenticationRequest;
import com.pecacm.backend.response.AuthenticationResponse;
import com.pecacm.backend.response.ErrorResponse;
import com.pecacm.backend.services.EmailService;
import com.pecacm.backend.services.JwtService;
import com.pecacm.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    private EmailService emailService;

    private UserService userService;

    private JwtService jwtService;

    private AuthenticationManager authenticationManager;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(EmailService emailService, UserService userService, JwtService jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.emailService = emailService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<AuthenticationResponse> registerUser(@RequestBody User user) {
        User newUser = userService.addUser(user, passwordEncoder);
        String jwtToken = jwtService.generateToken(user);
        emailService.sendVerificationEmail(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthenticationResponse(jwtToken, newUser));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> loginUser(@RequestBody AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getEmail(), request.getPassword()
            ));
        }
        catch (BadCredentialsException e) {
            throw new AcmException("Incorrect email or password", HttpStatus.UNAUTHORIZED);
        }
        User user = userService.loadUserByUsername(request.getEmail());
        if(!user.getVerified()) {
            emailService.sendVerificationEmail(user);
        }
        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthenticationResponse(jwtToken, user));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam UUID token) {
        userService.verifyUser(token);
        return ResponseEntity.ok("Verification successful!");
    }

    @PostMapping("/assignRole")
    public ResponseEntity<String> assignRole(@RequestBody AssignRoleRequest assignRoleRequest) {
        return ResponseEntity.ok(userService.changeRole(assignRoleRequest));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/rank")
    public ResponseEntity<Long> getRank(@RequestParam @NonNull Integer score) {
        return ResponseEntity.ok(userService.getRank(score));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<User>> getLeaderboard() {
        return ResponseEntity.ok(userService.getLeaderboard());
    }

    @ExceptionHandler(AcmException.class)
    public ResponseEntity<ErrorResponse> handleException(AcmException e) {
        return ResponseEntity.status(e.getStatus()).body(e.toErrorResponse());
    }
}
