package Ecommerce.service.Authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * NOTE: this is a stub. It logs the reset link instead of sending real
 * email — there's no transactional-email provider (SendGrid/SES/etc.)
 * wired up. Choosing and configuring one is a real infrastructure
 * decision (account, API key, verified sender domain) that shouldn't be
 * faked here. IEmailService exists specifically so that decision can be
 * made later without touching PasswordResetService or anything that calls
 * it — swap this class for a real implementation and nothing else changes.
 * For local/demo use: call POST /auth/forgot-password, then check the
 * backend logs for the reset link.
 */
@Slf4j
@Service
public class ConsoleEmailService implements IEmailService {

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        log.info("Password reset requested for {}. Reset link (valid for a limited time): {}", toEmail, resetLink);
    }
}
