package com.example.internshipmanagement.service;

import com.example.internshipmanagement.config.TokenBlacklist;
import com.example.internshipmanagement.dto.request.auth.ChangePasswordRequest;
import com.example.internshipmanagement.dto.request.auth.LoginRequest;
import com.example.internshipmanagement.dto.request.auth.RegisterRequest;
import com.example.internshipmanagement.dto.response.auth.LoginResponse;
import com.example.internshipmanagement.dto.response.user.UserResponse;
import com.example.internshipmanagement.entity.Student;
import com.example.internshipmanagement.entity.User;
import com.example.internshipmanagement.entity.enums.AuthProvider;
import com.example.internshipmanagement.entity.enums.Role;
import com.example.internshipmanagement.exception.ResourceConflictException;
import com.example.internshipmanagement.exception.ResourceNotFoundException;
import com.example.internshipmanagement.repository.StudentRepository;
import com.example.internshipmanagement.repository.UserRepository;
import com.example.internshipmanagement.service.impl.AuthServiceImpl;
import com.example.internshipmanagement.ulti.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private HttpServletRequest httpServletRequest;
    @Mock private TokenBlacklist tokenBlacklist;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .username("john")
                .passwordHash("hashedOldPassword")
                .fullName("John Doe")
                .email("john@example.com")
                .phoneNumber("0123456789")
                .role(Role.STUDENT)
                .provider(AuthProvider.LOCAL)
                .isActive(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String username) {
        Authentication auth = UsernamePasswordAuthenticationToken
                .authenticated(username, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ==================== login ====================

    @Test
    void login_ShouldReturnLoginResponse_WhenCredentialsValid() {
        LoginRequest request = new LoginRequest("john", "password123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated("john", null, List.of()));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("john", "STUDENT")).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo(1);
        assertThat(response.getUsername()).isEqualTo("john");
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void login_ShouldThrowIllegalArgumentException_WhenBadCredentials() {
        LoginRequest request = new LoginRequest("john", "wrongPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ten dang nhap hoac mat khau khong chinh xac");

        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_ShouldThrowIllegalArgumentException_WhenAccountDisabled() {
        LoginRequest request = new LoginRequest("john", "password123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("Disabled"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tai khoan da bi vo hieu hoa");

        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_ShouldThrowResourceNotFoundException_WhenUserNotFoundAfterAuthentication() {
        LoginRequest request = new LoginRequest("ghost", "password123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated("ghost", null, List.of()));
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(jwtUtil);
    }

    // ==================== register ====================

    private RegisterRequest registerRequest() {
        return new RegisterRequest(
                "newuser", "password123", "New User", "newuser@example.com", "0987654321");
    }

    @Test
    void register_ShouldCreateUserAndStudent_WhenDataValid() {
        RegisterRequest request = registerRequest();
        when(userRepository.existsByUsernameOrEmail("newuser", "newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setUserId(10);
            return saved;
        });
        when(jwtUtil.generateToken("newuser", "STUDENT")).thenReturn("new-jwt-token");

        LoginResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("new-jwt-token");
        assertThat(response.getUserId()).isEqualTo(10);
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getRole()).isEqualTo(Role.STUDENT);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPasswordHash()).isEqualTo("encodedPassword");
        assertThat(savedUser.getRole()).isEqualTo(Role.STUDENT);
        assertThat(savedUser.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(savedUser.getEmailVerified()).isFalse();
        assertThat(savedUser.getIsActive()).isTrue();

        // Tu dong tao Student record voi studentCode = "SV" + userId
        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studentCaptor.capture());
        assertThat(studentCaptor.getValue().getStudentCode()).isEqualTo("SV10");
    }

    @Test
    void register_ShouldThrowResourceConflictException_WhenUsernameExists() {
        RegisterRequest request = registerRequest();
        when(userRepository.existsByUsernameOrEmail("newuser", "newuser@example.com")).thenReturn(true);
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Ten dang nhap da ton tai");

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(studentRepository);
    }

    @Test
    void register_ShouldThrowResourceConflictException_WhenEmailExists() {
        RegisterRequest request = registerRequest();
        when(userRepository.existsByUsernameOrEmail("newuser", "newuser@example.com")).thenReturn(true);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Email da ton tai");

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(studentRepository);
    }

    // ==================== getCurrentUser ====================

    @Test
    void getCurrentUser_ShouldReturnUserResponse_WhenAuthenticated() {
        authenticateAs("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        UserResponse response = authService.getCurrentUser();

        assertThat(response.getUserId()).isEqualTo(1);
        assertThat(response.getUsername()).isEqualTo("john");
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo(Role.STUDENT);
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    void getCurrentUser_ShouldThrowIllegalArgumentException_WhenNotAuthenticated() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nguoi dung chua dang nhap");
    }

    @Test
    void getCurrentUser_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        authenticateAs("ghost");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ghost");
    }

    // ==================== changePassword ====================

    @Test
    void changePassword_ShouldUpdatePassword_WhenCurrentPasswordCorrect() {
        authenticateAs("john");
        ChangePasswordRequest request =
                new ChangePasswordRequest("oldPassword", "newPassword", "newPassword");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "hashedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("hashedNewPassword");

        authService.changePassword(request);

        assertThat(user.getPasswordHash()).isEqualTo("hashedNewPassword");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_ShouldThrowIllegalArgumentException_WhenCurrentPasswordWrong() {
        authenticateAs("john");
        ChangePasswordRequest request =
                new ChangePasswordRequest("wrongOld", "newPassword", "newPassword");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOld", "hashedOldPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mat khau hien tai khong chinh xac");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_ShouldThrowIllegalArgumentException_WhenConfirmPasswordMismatch() {
        ChangePasswordRequest request =
                new ChangePasswordRequest("oldPassword", "newPassword", "differentPassword");

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mat khau moi va xac nhan mat khau khong khop");

        verifyNoInteractions(userRepository);
    }

    @Test
    void changePassword_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        authenticateAs("ghost");
        ChangePasswordRequest request =
                new ChangePasswordRequest("oldPassword", "newPassword", "newPassword");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any(User.class));
    }
}
