package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.constant.Role;
import com.swpts.enpracticebe.dto.request.admin.UserFilterRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.AdminUserDetailResponse;
import com.swpts.enpracticebe.dto.response.admin.AdminUserListResponse;
import com.swpts.enpracticebe.entity.User;
import com.swpts.enpracticebe.repository.*;
import com.swpts.enpracticebe.service.AdminUserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final IeltsTestAttemptRepository ieltsTestAttemptRepository;
    private final SpeakingAttemptRepository speakingAttemptRepository;
    private final WritingSubmissionRepository writingSubmissionRepository;
    private final VocabularyRecordRepository vocabularyRecordRepository;
    private final ReviewSessionRepository reviewSessionRepository;

    @Override
    @Cacheable(value = "adminUserList", key = "#filter.page + '-' + #filter.size + '-' + #filter.search + '-' + #filter.role + '-' + #filter.isActive")
    public PageResponse<AdminUserListResponse> listUsers(UserFilterRequest filter) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String pattern = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("displayName")), pattern)
                ));
            }

            if (filter.getRole() != null && !filter.getRole().isBlank()) {
                predicates.add(cb.equal(root.get("role"), Role.valueOf(filter.getRole())));
            }

            if (filter.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageable = PageRequest.of(
                filter.getPage(), filter.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<User> page = userRepository.findAll(spec, pageable);

        List<AdminUserListResponse> items = page.getContent().stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminUserListResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(items)
                .build();
    }

    @Override
    @Cacheable(value = "adminUserDetail", key = "#userId")
    public AdminUserDetailResponse getUserDetail(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        long ieltsAttempts = ieltsTestAttemptRepository.countByUserId(userId);
        long speakingAttempts = speakingAttemptRepository.countByUserId(userId);
        long writingSubmissions = writingSubmissionRepository.countByUserId(userId);
        long vocabRecords = vocabularyRecordRepository.findByUserIdOrderByTestedAtDesc(userId).size();
        long reviewSessions = reviewSessionRepository.countByUserId(userId);

        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole().name())
                .isActive(user.isActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .totalIeltsAttempts(ieltsAttempts)
                .totalSpeakingAttempts(speakingAttempts)
                .totalWritingSubmissions(writingSubmissions)
                .totalVocabularyRecords(vocabRecords)
                .totalReviewSessions(reviewSessions)
                .build();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "adminUserList", allEntries = true),
            @CacheEvict(value = "adminUserDetail", key = "#userId")
    })
    public void changeRole(UUID userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRole(Role.valueOf(role));
        userRepository.save(user);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "adminUserList", allEntries = true),
            @CacheEvict(value = "adminUserDetail", key = "#userId")
    })
    public void toggleStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    private AdminUserListResponse toListResponse(User user) {
        return AdminUserListResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole().name())
                .isActive(user.isActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
