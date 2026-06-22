package com.petcare.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.user.entity.PhoneBlacklist;
import com.petcare.user.mapper.PhoneBlacklistMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for the phone blacklist with progressive ban support.
 *
 * <p>Each ban is a new row (history preserved). Ban level escalates by the count
 * of prior ban records for that phone:
 * <ul>
 *   <li>level 1 → 1 day</li>
 *   <li>level 2 → 3 days</li>
 *   <li>level 3 → 7 days</li>
 *   <li>level 4 → 30 days</li>
 *   <li>level 5 → 365 days</li>
 *   <li>level 6+ → permanent (ban_until = null)</li>
 * </ul>
 *
 * <p>Expiry is lazy: {@link #isPhoneBanned(String)} returns false if the only
 * ACTIVE record has passed its ban_until. Expired records are flipped to EXPIRED
 * on read so subsequent checks are fast.
 */
@Service
public class PhoneBlacklistService extends ServiceImpl<PhoneBlacklistMapper, PhoneBlacklist> {

    /** Ban duration in days per ban level (index 0 = level 1). */
    private static final int[] BAN_DAYS_BY_LEVEL = {1, 3, 7, 30, 365};
    private static final int PERMANENT_LEVEL = 6;

    /** Whether the given phone is currently banned (active AND not expired). */
    public boolean isPhoneBanned(String phone) {
        PhoneBlacklist active = getActiveBan(phone);
        return active != null;
    }

    /**
     * Get the currently active ban record for a phone, or null if not banned.
     * Lazily expires records whose ban_until has passed.
     */
    public PhoneBlacklist getActiveBan(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        List<PhoneBlacklist> active = this.list(new LambdaQueryWrapper<PhoneBlacklist>()
                .eq(PhoneBlacklist::getPhone, phone)
                .eq(PhoneBlacklist::getStatus, "ACTIVE")
                .orderByDesc(PhoneBlacklist::getBanLevel));
        for (PhoneBlacklist b : active) {
            // Permanent ban (ban_until null) never expires.
            if (b.getBanUntil() == null) {
                return b;
            }
            // Temporary ban still within window.
            if (b.getBanUntil().isAfter(LocalDateTime.now())) {
                return b;
            }
            // Expired — flip to EXPIRED so future checks skip it.
            lazilyExpire(b);
        }
        return null;
    }

    @Transactional
    protected void lazilyExpire(PhoneBlacklist b) {
        b.setStatus("EXPIRED");
        b.setUnbanTime(LocalDateTime.now());
        this.updateById(b);
    }

    /**
     * Add a ban for the phone, escalating the level based on prior ban history.
     *
     * @return the created ban record (with computed level/days/until)
     */
    @Transactional
    public PhoneBlacklist banPhone(String phone, Long userId, String reason, Long operatorId) {
        // Count prior bans (all records regardless of status) to compute next level
        long priorCount = this.count(new LambdaQueryWrapper<PhoneBlacklist>()
                .eq(PhoneBlacklist::getPhone, phone));
        int level = (int) Math.min(priorCount + 1, PERMANENT_LEVEL);
        Integer days = (level >= PERMANENT_LEVEL) ? null : BAN_DAYS_BY_LEVEL[level - 1];
        LocalDateTime until = (days == null) ? null : LocalDateTime.now().plusDays(days);

        // Expire any currently-active ban for this phone before adding the new one,
        // so at most one ACTIVE record exists per phone.
        this.list(new LambdaQueryWrapper<PhoneBlacklist>()
                        .eq(PhoneBlacklist::getPhone, phone)
                        .eq(PhoneBlacklist::getStatus, "ACTIVE"))
                .forEach(this::lazilyExpire);

        PhoneBlacklist entry = new PhoneBlacklist();
        entry.setPhone(phone);
        entry.setUserId(userId);
        entry.setReason(reason);
        entry.setOperatorId(operatorId);
        entry.setStatus("ACTIVE");
        entry.setBanLevel(level);
        entry.setBanDays(days);
        entry.setBanUntil(until);
        this.save(entry);
        return entry;
    }

    /**
     * Admin-initiated unban: expire the active ban record (history preserved).
     */
    @Transactional
    public void unbanPhone(String phone) {
        this.list(new LambdaQueryWrapper<PhoneBlacklist>()
                        .eq(PhoneBlacklist::getPhone, phone)
                        .eq(PhoneBlacklist::getStatus, "ACTIVE"))
                .forEach(b -> {
                    b.setStatus("UNBANNED");
                    b.setUnbanTime(LocalDateTime.now());
                    this.updateById(b);
                });
    }

    /** Human-readable description of the next ban level for a phone. */
    public String describeNextBan(String phone) {
        long priorCount = this.count(new LambdaQueryWrapper<PhoneBlacklist>()
                .eq(PhoneBlacklist::getPhone, phone));
        int level = (int) Math.min(priorCount + 1, PERMANENT_LEVEL);
        if (level >= PERMANENT_LEVEL) {
            return "第" + level + "次封禁：永久封禁";
        }
        int days = BAN_DAYS_BY_LEVEL[level - 1];
        return "第" + level + "次封禁：" + days + "天";
    }

    /** Remaining duration string for an active ban, or null if permanent/none. */
    public String describeRemaining(PhoneBlacklist ban) {
        if (ban == null) return null;
        if (ban.getBanUntil() == null) return "永久封禁";
        long hours = Duration.between(LocalDateTime.now(), ban.getBanUntil()).toHours();
        if (hours <= 0) return "即将解封";
        if (hours < 24) return "剩余" + hours + "小时";
        return "剩余" + (hours / 24) + "天";
    }
}
