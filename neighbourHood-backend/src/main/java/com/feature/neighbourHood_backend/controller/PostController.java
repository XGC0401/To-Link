package com.feature.neighbourHood_backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.feature.neighbourHood_backend.model.CustomUserDetails;
import com.feature.neighbourHood_backend.model.DTO.ApiResponse;
import com.feature.neighbourHood_backend.model.DTO.PostCommentCreateRequestDTO;
import com.feature.neighbourHood_backend.model.DTO.PostCommentResponseDTO;
import com.feature.neighbourHood_backend.model.DTO.createPostDTO;
import com.feature.neighbourHood_backend.model.entity.PhotoEntity;
import com.feature.neighbourHood_backend.model.entity.PostEntity;
import com.feature.neighbourHood_backend.service.PhotoService;
import com.feature.neighbourHood_backend.service.PostService;
import com.feature.neighbourHood_backend.service.SupabaseStorageService;
import com.feature.neighbourHood_backend.util.jwtUtil;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/post")
public class PostController {
    private final SupabaseStorageService storageService;
    private final PostService postService;
    private final PhotoService photoService;

    public PostController(SupabaseStorageService storageService, PostService postService, PhotoService photoService,
            jwtUtil jwtUtil) {
        this.storageService = storageService;
        this.postService = postService;
        this.photoService = photoService;
    }

    @PostMapping("/create-post")
    public ResponseEntity<ApiResponse> createRequest(@AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute createPostDTO request) {
        PostEntity post = postService.createRequest(request.getTitle(), request.getContent(), request.getType(),
                userDetails.getUuid(),
                request.getRedeemPoints(), request.getRequestType(), request.getPaymentMethod(),
                request.getIsImportant(), request.getStartTime(), request.getEndTime());

        if (post == null) {
            return ResponseEntity.status(400)
                .body(new ApiResponse<>("400", false, null, "Invalid post payload"));
        }

        List<String> urls = new ArrayList<>();
        if (request.getFiles() != null) {
            for (MultipartFile file : request.getFiles()) {
                String url = storageService.uploadFile(file, "request");
            if (url != null && url.startsWith("http"))
                    urls.add(url);
            }
        }
        List<PhotoEntity> photos = photoService.savePhoto(urls, post);
        postService.connectPhotos(post.getId(), photos);
        return ResponseEntity.status(200)
                .body(new ApiResponse<>("200", true, post.getId(), "Request created successfully"));
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse> getRequest(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<PostEntity> posts = postService.findAllVisible(userDetails.getUuid());
        if (posts != null) {
            return ResponseEntity.status(200).body(new ApiResponse<>(true, posts, "success"));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, null, "fail"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PostEntity post = postService.findById(id);
        if (post != null && postService.isVisibleForUser(post, userDetails.getUuid())) {
            return ResponseEntity.status(200).body(new ApiResponse<>(true, post, "success"));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, null, "fail"));
        }
    }

    @PostMapping("/accept-post")
    public ResponseEntity<ApiResponse> acceptPost(@RequestBody Long postID, @RequestBody UUID uuid) {
        int response = postService.acceptRequest(postID, uuid);
        if (response == 1) {
            return ResponseEntity.status(200).body(new ApiResponse<>(true, "success"));
        } else if (response == 2) {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, "fail to find corresponding user"));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, "fail to find the post"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PostEntity post = postService.findById(id);
        if (post == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, false, "post not found"));
        }

        boolean isOwner = post.getUser() != null && post.getUser().getUuid() != null
                && post.getUser().getUuid().equals(userDetails.getUuid());
        boolean isAdminRole = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isAdminEmail = "admin@gmail.com".equalsIgnoreCase(userDetails.getEmail());

        if (!isOwner && !isAdminRole && !isAdminEmail) {
            return ResponseEntity.status(403).body(new ApiResponse<>(false, false, "permission denied"));
        }

        photoService.deleteByPostId(id);
        boolean deleted = postService.deletePost(id);

        if (!deleted) {
            return ResponseEntity.status(500).body(new ApiResponse<>(false, false, "delete failed"));
        }

        return ResponseEntity.status(200).body(new ApiResponse<>(true, true, "post deleted"));
    }

    @PostMapping("/like-post")
    public ResponseEntity<ApiResponse> likePost(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody JsonNode payload) {
        Long postID = extractPostId(payload);
        if (postID == null) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, false, "postID is required"));
        }

        int response = postService.likePost(postID, userDetails.getUuid());
        if (response == 1) {
            return ResponseEntity.status(200).body(new ApiResponse<>(true, true,"success"));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>(false,false, "fail to find corresponding user or post"));
        }
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse> getPostComments(@PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PostEntity post = postService.findById(id);
        if (post == null || !postService.isVisibleForUser(post, userDetails.getUuid())) {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, null, "fail"));
        }

        List<PostCommentResponseDTO> comments = postService.getPostComments(id);
        return ResponseEntity.status(200).body(new ApiResponse<>(true, comments, "success"));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse> createPostComment(@PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostCommentCreateRequestDTO payload) {
        PostEntity post = postService.findById(id);
        if (post == null || !postService.isVisibleForUser(post, userDetails.getUuid())) {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, null, "fail"));
        }

        PostCommentResponseDTO created = postService.createPostComment(id, userDetails.getUuid(),
                payload == null ? null : payload.getContent());

        if (created == null) {
            return ResponseEntity.status(400).body(new ApiResponse<>(false, null, "comment is required"));
        }

        return ResponseEntity.status(200).body(new ApiResponse<>(true, created, "success"));
    }

    private Long extractPostId(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return null;
        }

        if (payload.isNumber()) {
            return payload.longValue();
        }

        if (payload.isTextual()) {
            try {
                return Long.parseLong(payload.textValue());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        JsonNode idNode = payload.get("postID");
        if (idNode == null) {
            idNode = payload.get("postId");
        }
        if (idNode == null || idNode.isNull()) {
            return null;
        }

        if (idNode.isNumber()) {
            return idNode.longValue();
        }
        if (idNode.isTextual()) {
            try {
                return Long.parseLong(idNode.textValue());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}