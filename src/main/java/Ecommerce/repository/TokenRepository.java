package Ecommerce.repository;

import Ecommerce.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenHashAndRevokedFalse(String tokenHash);

    @Query("select t from Token t where t.user.id = :userId and t.revoked = false")
    List<Token> findAllValidTokensByUser(Long userId);
}
