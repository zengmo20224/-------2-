package com.petcare.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for table `phone_blacklist`.
 * A phone in this list cannot be used to register a new account, even if the
 * original account was deleted. Decoupled from the user table so banning survives
 * account deletion.
 *
 * Note: does NOT extend BaseEntity — this table has only id + create_time
 * (no update_time, no logical delete).
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("phone_blacklist")
public class PhoneBlacklist {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("phone")
    private String phone;

    @TableField("user_id")
    private Long userId;

    @TableField("reason")
    private String reason;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("status")
    private String status;

    @TableField("ban_level")
    private Integer banLevel;

    @TableField("ban_days")
    private Integer banDays;

    @TableField("ban_until")
    private LocalDateTime banUntil;

    @TableField("unban_time")
    private LocalDateTime unbanTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
