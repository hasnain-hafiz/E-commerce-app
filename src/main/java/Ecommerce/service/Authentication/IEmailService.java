package Ecommerce.service.Authentication;

public interface IEmailService {
    void sendPasswordResetEmail(String toEmail, String resetLink);
}
