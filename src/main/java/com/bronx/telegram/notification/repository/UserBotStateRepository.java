package com.bronx.telegram.notification.repository;

import com.bronx.telegram.notification.model.entity.UserBotState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBotStateRepository extends JpaRepository<UserBotState, String> {

}
