package com.bronx.notification.repository;
import com.bronx.notification.model.entity.Subscription;
import com.bronx.notification.model.entity.SubscriptionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {

    @Query("select s from SubscriptionHistory s where s.subscription = ?1")
    Page<SubscriptionHistory> findAllBySubscription(Subscription subscription, Pageable pageable);
}
