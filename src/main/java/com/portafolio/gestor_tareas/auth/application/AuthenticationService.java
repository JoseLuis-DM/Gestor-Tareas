package com.portafolio.gestor_tareas.auth.application;

import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.users.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public AuthenticationResponse register(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) throw new NotFoundException("error"){


        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

    }

}
