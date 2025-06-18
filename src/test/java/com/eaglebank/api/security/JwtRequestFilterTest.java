package com.eaglebank.api.security;

import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtRequestFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails userDetails;
    private User user;
    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_USERNAME = "test@example.com";
    private static final String TEST_USER_ID = "usr-12345678";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();

        user = new User();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);

        userDetails = org.springframework.security.core.userdetails.User
                .withUsername(TEST_USERNAME)
                .password("password")
                .authorities(new ArrayList<>())
                .build();
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);
        when(jwtUtil.extractUsername(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(jwtUtil.validateToken(TEST_TOKEN)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // Act
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(TEST_USERNAME, SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals(TEST_USER_ID, request.getAttribute(JwtRequestFilter.AUTHENTICATED_USER_ID));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);
        when(jwtUtil.extractUsername(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(jwtUtil.validateToken(TEST_TOKEN)).thenReturn(false);

        // Act
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNoToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Act
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidTokenFormat_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "InvalidFormat " + TEST_TOKEN);

        // Act
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithExpiredToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);
        when(jwtUtil.extractUsername(TEST_TOKEN)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // Act
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }


    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
