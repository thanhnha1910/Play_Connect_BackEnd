package fpt.aptech.management_field.security.services;

import fpt.aptech.management_field.models.*;
import fpt.aptech.management_field.payload.request.OwnerRegistrationRequest;
import fpt.aptech.management_field.repositories.OwnerRepository;
import fpt.aptech.management_field.repositories.RoleRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import fpt.aptech.management_field.services.NotificationService;
import fpt.aptech.management_field.services.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final NotificationService notificationService;
    private final EmailService emailService;
    
    public AuthService(UserRepository userRepository, 
                      OwnerRepository ownerRepository,
                      RoleRepository roleRepository, 
                      PasswordEncoder encoder,
                      NotificationService notificationService,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.notificationService = notificationService;
        this.emailService = emailService;
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
        // Khởi tạo memberLevel và bookingCount
        user.setMemberLevel(0);
        user.setBookingCount(0);
        
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
        
        // Send notifications to all admins
        try {
            List<User> adminUsers = userRepository.findByRoles_Name(ERole.ROLE_ADMIN);
            
            for (User admin : adminUsers) {
                // Create in-app notification
                Notification notification = new Notification();
                notification.setRecipient(admin);
                notification.setTitle("Yêu cầu đối tác mới");
                notification.setContent("Có yêu cầu đối tác mới từ " + savedUser.getFullName() + " (" + request.getBusinessName() + ") cần được duyệt.");
                notification.setType("OWNER_REGISTRATION");
                notification.setRelatedEntityId(savedUser.getId());
                notification.setCreatedAt(java.time.LocalDateTime.now());
                notification.setIsRead(false);
                
                notificationService.createNotification(notification);
                
                // Send email notification to admin
                String subject = "Yêu cầu đối tác mới cần duyệt - PlayerConnect";
                String content = "Xin chào,\n\n" +
                               "Có yêu cầu đối tác mới cần được duyệt:\n\n" +
                               "Tên: " + savedUser.getFullName() + "\n" +
                               "Email: " + savedUser.getEmail() + "\n" +
                               "Tên doanh nghiệp: " + request.getBusinessName() + "\n" +
                               "Số điện thoại: " + savedUser.getPhoneNumber() + "\n" +
                               "Địa chỉ: " + savedUser.getAddress() + "\n\n" +
                               "Vui lòng đăng nhập vào hệ thống admin để duyệt yêu cầu này.\n\n" +
                               "Trân trọng,\nHệ thống PlayerConnect";
                
                emailService.sendEmail(admin.getEmail(), subject, content);
            }
            
            // Send confirmation email to owner
            String ownerSubject = "Yêu cầu đối tác đã được gửi thành công - PlayerConnect";
            String ownerContent = "Xin chào " + savedUser.getFullName() + ",\n\n" +
                                "Cảm ơn bạn đã đăng ký trở thành đối tác của PlayerConnect!\n\n" +
                                "Thông tin đăng ký của bạn:\n" +
                                "Tên doanh nghiệp: " + request.getBusinessName() + "\n" +
                                "Email: " + savedUser.getEmail() + "\n\n" +
                                "Yêu cầu của bạn đang được xem xét và sẽ được phản hồi trong thời gian sớm nhất.\n" +
                                "Chúng tôi sẽ thông báo kết quả qua email này.\n\n" +
                                "Trân trọng,\nĐội ngũ PlayerConnect";
            
            emailService.sendEmail(savedUser.getEmail(), ownerSubject, ownerContent);
            
        } catch (Exception e) {
            // Log error but don't fail the registration process
            System.err.println("Failed to send notifications: " + e.getMessage());
        }
        
        return savedUser;
    }
}