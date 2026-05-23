package com.nikhil.project.uber.uberApp.services.impl;

import com.nikhil.project.uber.uberApp.dto.SignupDto;
import com.nikhil.project.uber.uberApp.dto.UserDto;
import com.nikhil.project.uber.uberApp.entities.Driver;
import com.nikhil.project.uber.uberApp.entities.User;
import com.nikhil.project.uber.uberApp.entities.enums.Role;
import com.nikhil.project.uber.uberApp.exceptions.ResourceNotFoundException;
import com.nikhil.project.uber.uberApp.exceptions.RuntimeConflictException;
import com.nikhil.project.uber.uberApp.repositories.UserRepository;
import com.nikhil.project.uber.uberApp.security.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RiderServiceImpl riderService;

    @Mock
    private WalletServiceImpl walletService;

    @Mock
    private DriverServiceImpl driverService;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRoles(new HashSet<>(Set.of(Role.RIDER)));
    }

    @Test
    void testLogin_whenSuccess() {
//        arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");

//        act
        String[] tokens = authService.login(user.getEmail(), user.getPassword());

//        assert
        assertThat(tokens).hasSize(2);
        assertThat(tokens[0]).isEqualTo("accessToken");
        assertThat(tokens[1]).isEqualTo("refreshToken");
    }

    @Test
    void testSignup_whenSuccess() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        SignupDto signupDto = new SignupDto();
        signupDto.setEmail("test@example.com");
        signupDto.setPassword("password");
        UserDto userDto = authService.signup(signupDto);

        // Assert
        assertThat(userDto).isNotNull();
        assertThat(userDto.getEmail()).isEqualTo(signupDto.getEmail());
        verify(riderService).createNewRider(any(User.class));
        verify(walletService).createNewWallet(any(User.class));
    }

    @Test
    void testSignup_whenEmailAlreadyExists_thenThrowsConflict() {
        SignupDto signupDto = new SignupDto();
        signupDto.setEmail(user.getEmail());

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(RuntimeConflictException.class, () -> authService.signup(signupDto));
        verify(userRepository, never()).save(any());
        verifyNoInteractions(riderService, walletService);
    }

    @Test
    void testOnboardNewDriver_whenSuccess() {
        Driver createdDriver = Driver.builder()
                .id(10L)
                .user(user)
                .vehicleId("KA01AB1234")
                .available(true)
                .rating(0.0)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(driverService.createNewDriver(any(Driver.class))).thenReturn(createdDriver);

        var driverDto = authService.onboardNewDriver(user.getId(), "KA01AB1234");

        assertThat(driverDto.getVehicleId()).isEqualTo("KA01AB1234");
        assertThat(user.getRoles()).contains(Role.DRIVER);
        verify(driverService).createNewDriver(any(Driver.class));
    }

    @Test
    void testOnboardNewDriver_whenUserMissing_thenThrowsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.onboardNewDriver(99L, "ABC"));
        verifyNoInteractions(driverService);
    }

    @Test
    void testOnboardNewDriver_whenAlreadyDriver_thenThrowsConflict() {
        user.getRoles().add(Role.DRIVER);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(RuntimeConflictException.class, () -> authService.onboardNewDriver(user.getId(), "ABC"));
        verify(driverService, never()).createNewDriver(any());
    }

    @Test
    void testRefreshToken_whenSuccess() {
        when(jwtService.getUserIdFromToken("refreshToken")).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("newAccessToken");

        String token = authService.refreshToken("refreshToken");

        assertThat(token).isEqualTo("newAccessToken");
    }

    @Test
    void testRefreshToken_whenUserMissing_thenThrowsNotFound() {
        when(jwtService.getUserIdFromToken("refreshToken")).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.refreshToken("refreshToken"));
    }
}
