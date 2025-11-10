package com.project.apsas.service.impl;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.apsas.dto.request.IntrospectRequest;
import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.response.IntrospecResponse;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.entity.User;
import com.project.apsas.mapper.UserMapper;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AuthService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    UserMapper userMapper;

    @NonFinal
    @Value("${jwt.signerKey:CHANGE_ME_SECRET}")
    String SIGNER_KEY;

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getName())
                .issuer("apsas.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .claim("scope", buildScope(user))
                .build();

        try {
            JWSObject jws = new JWSObject(header, new Payload(claims.toJSONObject()));
            jws.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jws.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner sj = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(r -> sj.add(r.getName()));
        }
        return sj.toString();
    }

    @Override
    public IntrospecResponse introspect(IntrospectRequest req) throws JOSEException, ParseException {
        SignedJWT jwt = SignedJWT.parse(req.getToken());
        boolean verified = jwt.verify(new MACVerifier(SIGNER_KEY.getBytes()));
        Date exp = jwt.getJWTClaimsSet().getExpirationTime();
        return IntrospecResponse.builder()
                .valid(verified && exp.after(new Date()))
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Incorrect password");
        }
        return userMapper.toLoginResponse(user);
    }
}
