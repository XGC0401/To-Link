package com.feature.neighbourHood_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.feature.neighbourHood_backend.model.entity.PhotoEntity;
import com.feature.neighbourHood_backend.model.entity.PostEntity;
import com.feature.neighbourHood_backend.model.entity.User;
import com.feature.neighbourHood_backend.repository.PostRepository;
import com.feature.neighbourHood_backend.repository.UserRepository;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public PostEntity createRequest(String title, String content, int type, UUID userId, int redeemPoints,
            int request_type, int payment_method, boolean is_important, LocalDateTime startTime,
            LocalDateTime endTime) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            PostEntity post = new PostEntity(title, content, type, user.get(), redeemPoints, request_type,
                    payment_method, is_important, startTime, endTime);
            post = postRepository.save(post);
            return post;
        }
        return null;
    }

    public PostEntity findById(Long id) {
        Optional<PostEntity> post = postRepository.findById(id);
        if (post.isPresent()) {
            return post.get();
        }
        return null;
    }

    public List<PostEntity> findAll() {
        return postRepository.findAll();
    }

    public List<PostEntity> findAllVisible(UUID currentUserId) {
        Optional<User> currentUser = userRepository.findById(currentUserId);
        if (currentUser.isEmpty()) {
            return postRepository.findAll();
        }

        Set<UUID> blockedByMe = currentUser.get().getBlockedUsers().stream()
                .map(User::getUuid)
                .collect(Collectors.toSet());

        return postRepository.findAll().stream()
                .filter(post -> post.getUser() != null && post.getUser().getUuid() != null)
                .filter(post -> {
                    UUID authorId = post.getUser().getUuid();
                    if (authorId.equals(currentUserId)) {
                        return true;
                    }
                    if (blockedByMe.contains(authorId)) {
                        return false;
                    }
                    return !post.getUser().getBlockedUsers().stream()
                            .map(User::getUuid)
                            .collect(Collectors.toSet())
                            .contains(currentUserId);
                })
                .collect(Collectors.toList());
    }

    public boolean isVisibleForUser(PostEntity post, UUID currentUserId) {
        if (post == null || post.getUser() == null || post.getUser().getUuid() == null) {
            return false;
        }
        UUID authorId = post.getUser().getUuid();
        if (authorId.equals(currentUserId)) {
            return true;
        }
        Optional<User> currentUser = userRepository.findById(currentUserId);
        if (currentUser.isEmpty()) {
            return true;
        }
        boolean blockedByMe = currentUser.get().getBlockedUsers().stream()
                .map(User::getUuid)
                .anyMatch(authorId::equals);
        if (blockedByMe) {
            return false;
        }
        return post.getUser().getBlockedUsers().stream()
                .map(User::getUuid)
                .noneMatch(currentUserId::equals);
    }

    public int acceptRequest(Long id, UUID uuid) {
        Optional<PostEntity> post = postRepository.findById(id);
        if (post.isPresent()) {
            Optional<User> user = userRepository.findById(uuid);
            if (user.isPresent()) {
                PostEntity postEntity = post.get();
                postEntity.setAcceptUser(user.get());
                postRepository.save(postEntity);
                return 1;
            } else {
                return 2;
            }
        }
        return 3;
    }

    public void connectPhotos(Long id, List<PhotoEntity> photos) {
        Optional<PostEntity> post = postRepository.findById(id);
        if (post.isPresent()) {
            for (PhotoEntity photo : photos) {
                post.get().addPhoto(photo);
                postRepository.save(post.get());
            }
        }
    }

    public boolean deletePost(Long id) {
        Optional<PostEntity> post = postRepository.findById(id);
        if (post.isPresent()) {
            postRepository.delete(post.get());
            return true;
        }
        return false;
    }

    public int likePost(Long postID, UUID userUuid){
        Optional<User> user = userRepository.findById(userUuid);
        Optional<PostEntity> post = postRepository.findById(postID);

        if(user.isPresent() && post.isPresent()){
            User tUser = user.get();
            PostEntity tPost = post.get();

            boolean liked = tUser.getLikePosts().stream()
                    .anyMatch(p -> p != null && p.getId() != null && p.getId().equals(postID));

            if (liked) {
                tUser.removeLike(tPost);
                tPost.removeLike(tUser);
            } else {
                tUser.addLike(tPost);
                tPost.addLike(tUser);
            }

            userRepository.save(tUser);
            postRepository.save(tPost);
            return 1;
        }
        return 2;
    }   
}
