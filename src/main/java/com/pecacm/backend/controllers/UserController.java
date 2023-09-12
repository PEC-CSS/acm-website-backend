package com.pecacm.backend.controllers;

import com.pecacm.backend.constants.Constants;
import com.pecacm.backend.entities.User;
import com.pecacm.backend.entities.VerificationToken;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.model.AssignRoleRequest;
import com.pecacm.backend.model.AuthenticationRequest;
import com.pecacm.backend.response.AuthenticationResponse;
import com.pecacm.backend.response.RegisterResponse;
import com.pecacm.backend.services.EmailService;
import com.pecacm.backend.services.JwtService;
import com.pecacm.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    private final EmailService emailService;

    private final UserService userService;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(EmailService emailService, UserService userService, JwtService jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.emailService = emailService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody User user) {
        userService.addUser(user, passwordEncoder);
//        emailService.sendVerificationEmail(newUser); REASON : too much latency
        VerificationToken token = emailService.getVerificationToken(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse(token.getToken().toString()));
    }

    @GetMapping
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<User> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PostMapping("/login")
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<AuthenticationResponse> loginUser(@RequestBody AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getEmail(), request.getPassword()
            ));
        } catch (BadCredentialsException e) {
            throw new AcmException("Incorrect email or password", HttpStatus.UNAUTHORIZED);
        }
        User user = userService.loadUserByUsername(request.getEmail());
        if (!user.getVerified()) {
            throw new AcmException("User not verified, please verify with the link sent to your email.", HttpStatus.UNAUTHORIZED);
        }
        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthenticationResponse(jwtToken, user));
    }

    @GetMapping("/verify")
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<String> verifyUser(@RequestParam UUID token) {
        userService.verifyUser(token);
        return ResponseEntity.ok("Verification successful!");
    }

    @PostMapping("/assign/role")
    @PreAuthorize(Constants.HAS_ROLE_CORE_AND_ABOVE)
    public ResponseEntity<String> assignRole(@RequestBody AssignRoleRequest assignRoleRequest) {
        return ResponseEntity.ok(userService.changeRole(assignRoleRequest));
    }

    @GetMapping("/{userId}")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<User> getUserById(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/rank")
    public ResponseEntity<Long> getRank(@RequestParam @NonNull Integer score) {
        return ResponseEntity.ok(userService.getRank(score));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<User>> getLeaderboard(@RequestParam @NonNull Integer pageSize,@RequestParam @NonNull Integer pageNum ) {
        return ResponseEntity.ok(userService.getLeaderboard(pageSize,pageNum));
    }
}
