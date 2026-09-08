package Ecommerce.repository;

import Ecommerce.model.Order;
import Ecommerce.utils.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    List<Order> findAllByUserId(Long userId);

    // NEW (Phase 2b): backs the "verified purchase" gate on reviews — a
    // user may only review a product they've actually ordered (in any
    // status other than the one excluded, typically CANCELLED).
    @Query("""
        SELECT COUNT(oi) > 0 FROM Order o JOIN o.orderItems oi
        WHERE o.user.id = :userId AND oi.product.id = :productId AND o.orderStatus <> :excludedStatus
        """)
    boolean hasPurchased(@Param("userId") Long userId,
                          @Param("productId") Long productId,
                          @Param("excludedStatus") OrderStatus excludedStatus);
}
