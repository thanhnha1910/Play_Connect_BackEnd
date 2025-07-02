package fpt.aptech.management_field.security.services;

import fpt.aptech.management_field.models.*;
import fpt.aptech.management_field.payload.request.OwnerRegistrationRequest;
import fpt.aptech.management_field.repositories.OwnerRepository;
import fpt.aptech.management_field.repositories.RoleRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    
    public AuthService(UserRepository userRepository, 
                      OwnerRepository ownerRepository,
                      RoleRepository roleRepository, 
                      PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }
    
    @Transactional
    public User registerOwner(OwnerRegistrationRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setStatus(UserStatus.PENDING_APPROVAL); // Owner needs approval
        user.setEmailVerified(true); // Auto-verify for owners
        
        // Set ROLE_OWNER
        Set<Role> roles = new HashSet<>();
        Role ownerRole = roleRepository.findByName(ERole.ROLE_OWNER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(ownerRole);
        user.setRoles(roles);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Create and save owner
        Owner owner = new Owner();
        owner.setBusinessName(request.getBusinessName());
        owner.setUser(savedUser);
        ownerRepository.save(owner);
        
        return savedUser;
    }
}