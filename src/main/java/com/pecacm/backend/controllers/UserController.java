package com.pecacm.backend.controllers;

import com.pecacm.backend.constants.Constants;
import com.pecacm.backend.entities.*;
import com.pecacm.backend.enums.EventRole;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.model.AssignRoleRequest;
import com.pecacm.backend.model.AuthenticationRequest;
import com.pecacm.backend.response.AuthenticationResponse;
import com.pecacm.backend.response.ForgetPasswordResponse;
import com.pecacm.backend.response.RegisterResponse;
import com.pecacm.backend.response.UserEventDetails;
import com.pecacm.backend.services.ForgetPasswordService;
import com.pecacm.backend.services.VerificationService;
import com.pecacm.backend.services.JwtService;
import com.pecacm.backend.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    private final VerificationService verificationService;

    private final UserService userService;

    private final JwtService jwtService;

    private final ForgetPasswordService forgetPasswordService;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    public UserController(VerificationService verificationService, UserService userService, JwtService jwtService, ForgetPasswordService forgetPasswordService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.verificationService = verificationService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.forgetPasswordService = forgetPasswordService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody User user) {
        userService.addUser(user, passwordEncoder);
        VerificationToken token = verificationService.getVerificationToken(user);
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
    public ResponseEntity<List<User>> getLeaderboard(@RequestParam @Nullable Integer offset, @RequestParam @Nullable Integer pageSize) {
        if (offset == null) offset = 0;
        if (pageSize == null) pageSize = 20; // returning first 20 users

        if (offset < 0) throw new AcmException("offset cannot be < 0", HttpStatus.BAD_REQUEST);
        if (pageSize <= 0) throw new AcmException("pageSize must be >= 0", HttpStatus.BAD_REQUEST);

        return ResponseEntity.ok(userService.getLeaderboard(offset, pageSize));
    }

    @GetMapping("/leaderboard/{batch}")
    public ResponseEntity<List<User>> getLeaderboardByBatch(@PathVariable Integer batch, @RequestParam @Nullable Integer offset, @RequestParam @Nullable Integer pageSize) {
        if (offset == null) offset = 0;
        if (pageSize == null) pageSize = 20; // returning first 20 users

        if (offset < 0) throw new AcmException("offset cannot be < 0", HttpStatus.BAD_REQUEST);
        if (pageSize <= 0) throw new AcmException("pageSize must be >= 0", HttpStatus.BAD_REQUEST);

        List<User> users = userService.getLeaderboardByBatch(batch, offset, pageSize);
        return ResponseEntity.ok(users);
    }

    @PutMapping
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User updatedUser = userService.updateUser(user, email);
        return (updatedUser == null) ? ResponseEntity.badRequest().build() : ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/forget-password")
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<ForgetPasswordResponse> getForgetPasswordToken(String email){
        User user = userService.getUserByEmail(email);
        ForgetPasswordToken forgetPasswordToken = forgetPasswordService.getForgetPasswordToken(user);
        return ResponseEntity.status(HttpStatus.OK).body(new ForgetPasswordResponse(forgetPasswordToken.getToken().toString()));
    }

    @PostMapping("/forget-password")
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<AuthenticationResponse> changePassword(@RequestParam UUID token, @RequestBody String newPassword){
        ForgetPasswordToken forgetPasswordToken = forgetPasswordService.findByToken(token);
        if (forgetPasswordToken==null || forgetPasswordToken.getCreatedDate().isBefore(LocalDateTime.now().minusMinutes(10))){
            throw new AcmException("Invalid or expired token.", HttpStatus.BAD_REQUEST);
        }
        User user = forgetPasswordToken.getUser();
        userService.changePassword(user,newPassword);
        forgetPasswordService.deleteToken(forgetPasswordToken);
        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthenticationResponse(jwtToken, user));
    }

    @GetMapping("/transaction")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<List<Transaction>> getUserTransactions(@RequestParam @Nullable Integer offset, @RequestParam @Nullable Integer pageSize) {
        if (offset == null) offset = 0;
        if (pageSize == null) pageSize = 20; // returning first 20 transactions

        if (offset < 0) throw new AcmException("offset cannot be < 0", HttpStatus.BAD_REQUEST);
        if (pageSize <= 0) throw new AcmException("pageSize must be >= 0", HttpStatus.BAD_REQUEST);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getUserTransactions(email, offset, pageSize));
    }

    @GetMapping("/events")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<List<UserEventDetails>> getEventsForUser(@RequestParam @Nullable EventRole eventRole, @RequestParam @Nullable Integer pageSize, @RequestParam @Nullable Integer offset) {
        if (offset == null) offset = 0;
        if (pageSize == null) pageSize = 20; // returning first 20 users

        if (offset < 0) throw new AcmException("offset cannot be < 0", HttpStatus.BAD_REQUEST);
        if (pageSize <= 0) throw new AcmException("pageSize must be >= 0", HttpStatus.BAD_REQUEST);

        if (eventRole != null) {
            return ResponseEntity.ok(userService.getEventsForUserWithRole(eventRole, pageSize, offset));
        }

        return ResponseEntity.ok(userService.getEventsForUser(pageSize, offset));

    }
}
