package com.feature.neighbourHood_backend.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.feature.neighbourHood_backend.model.entity.UserPresence;

@Repository
public interface UserPresenceRepository extends JpaRepository<UserPresence, UUID> {
    List<UserPresence> findByUserIdIn(Collection<UUID> userIds);
}
