package com.gearup.userauth.service;

import com.gearup.userauth.model.User;
import com.gearup.userauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OTPService {
    private final UserRepository userRepository;
    private static final SecureRandom random = new SecureRandom();

    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Transactional
    public void setOtpForUser(User user, String otp) {
        user.setSetupOtp(otp);
        user.setSetupOtpExpiresAt(LocalDateTime.now().plusHours(24));
        user.setIsPasswordSet(false);
        userRepository.save(user);
    }

    public boolean validateOtp(User user, String otp) {
        if (user.getSetupOtp() == null || user.getSetupOtpExpiresAt() == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(user.getSetupOtpExpiresAt())) {
            return false;
        }

        return user.getSetupOtp().equals(otp);
    }

    @Transactional
    public void clearOtp(User user) {
        user.setSetupOtp(null);
        user.setSetupOtpExpiresAt(null);
        userRepository.save(user);
    }
}
