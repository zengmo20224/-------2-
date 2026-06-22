package com.petcare.notification.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.pagination.PageResponse;
import com.petcare.common.security.SecurityContextHelper;
import com.petcare.notification.dto.PublicAnnouncementResponse;
import com.petcare.notification.dto.UnreadCountResponse;
import com.petcare.notification.dto.UserNotificationResponse;
import com.petcare.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Notification and announcement endpoints.
 *
 * - Announcement reads are public (anonymous can view).
 * - User notification reads require authentication.
 */
@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ==================== Announcements (public) ====================

    @GetMapping("/announcements")
    public ResponseEntity<ApiResponse<List<PublicAnnouncementResponse>>> listAnnouncements(
            @RequestParam(defaultValue = "10") int limit) {
        List<PublicAnnouncementResponse> result = notificationService.listPublishedAnnouncements(limit);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/announcements/{id}")
    public ResponseEntity<ApiResponse<PublicAnnouncementResponse>> getAnnouncement(@PathVariable Long id) {
        PublicAnnouncementResponse result = notificationService.getPublishedAnnouncement(id);
        if (result == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "公告不存在");
        }
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ==================== User Notifications (authenticated) ====================

    @GetMapping("/user/notifications")
    public ResponseEntity<ApiResponse<PageResponse<UserNotificationResponse>>> listNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = resolveUserId();
        PageResponse<UserNotificationResponse> result = notificationService.listNotifications(userId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/user/notifications/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        Long userId = resolveUserId();
        UnreadCountResponse result = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/user/notifications/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        Long userId = resolveUserId();
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/user/notifications/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        Long userId = resolveUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private Long resolveUserId() {
        return SecurityContextHelper.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"));
    }
}
