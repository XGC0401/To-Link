package com.feature.neighbourHood_backend.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.feature.neighbourHood_backend.model.DTO.PresenceResponseDTO;
import com.feature.neighbourHood_backend.model.entity.User;
import com.feature.neighbourHood_backend.model.entity.UserPresence;
import com.feature.neighbourHood_backend.repository.UserPresenceRepository;
import com.feature.neighbourHood_backend.repository.UserRepository;

@Service
public class PresenceService {
    private static final Set<String> VALID_STATUSES = Set.of("online", "busy", "offline", "hidden");

    private final UserPresenceRepository userPresenceRepository;
    private final UserRepository userRepository;

    public PresenceService(UserPresenceRepository userPresenceRepository, UserRepository userRepository) {
        this.userPresenceRepository = userPresenceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PresenceResponseDTO updatePresence(User currentUser, String status) {
        String normalizedStatus = normalizeStatus(status);

        UserPresence presence = userPresenceRepository.findById(currentUser.getUuid()).orElseGet(() -> {
            UserPresence created = new UserPresence();
            created.setUserId(currentUser.getUuid());
            return created;
        });

        presence.setStatus(normalizedStatus);
        presence.setLastActiveAt(LocalDateTime.now());
        UserPresence saved = userPresenceRepository.save(presence);

        return PresenceResponseDTO.builder()
                .email(currentUser.getEmail())
                .status(resolveDisplayStatus(saved))
                .lastActiveAt(saved.getLastActiveAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PresenceResponseDTO> getPresencesByEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return List.of();
        }

        List<User> users = userRepository.findByEmailIn(emails);
        Map<UUID, User> userById = users.stream().collect(Collectors.toMap(User::getUuid, user -> user));

        List<UUID> userIds = users.stream().map(User::getUuid).toList();
        List<UserPresence> presences = userPresenceRepository.findByUserIdIn(userIds);

        Map<UUID, UserPresence> presenceByUserId = new HashMap<>();
        for (UserPresence presence : presences) {
            presenceByUserId.put(presence.getUserId(), presence);
        }

        List<PresenceResponseDTO> response = new ArrayList<>();
        for (User user : users) {
            UserPresence presence = presenceByUserId.get(user.getUuid());
            if (presence == null) {
                response.add(PresenceResponseDTO.builder()
                        .email(user.getEmail())
                        .status("offline")
                        .lastActiveAt(null)
                        .build());
                continue;
            }

            response.add(PresenceResponseDTO.builder()
                    .email(user.getEmail())
                    .status(resolveDisplayStatus(presence))
                    .lastActiveAt(presence.getLastActiveAt())
                    .build());
        }

        return response;
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "online";
        }

        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if (!VALID_STATUSES.contains(normalized)) {
            return "online";
        }
        return normalized;
    }

    private String resolveDisplayStatus(UserPresence presence) {
        String stored = normalizeStatus(presence.getStatus());
        if ("offline".equals(stored) || "hidden".equals(stored)) {
            return "offline";
        }
        if ("busy".equals(stored)) {
            return "busy";
        }

        if (presence.getLastActiveAt() == null) {
            return "offline";
        }

        long minutes = Duration.between(presence.getLastActiveAt(), LocalDateTime.now()).toMinutes();
        if (minutes <= 3) {
            return "active";
        }
        if (minutes <= 30) {
            return "online";
        }
        if (minutes <= 360) {
            return "inactive";
        }
        return "offline";
    }
}
