package com.petcare.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for table `ai_analysis_report`.
 * Does NOT extend BaseEntity because this table has no update_time or deleted field.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("ai_analysis_report")
public class AiAnalysisReport {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("report_type")
    private String reportType;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("raw_data_json")
    private String rawDataJson;

    @TableField("ai_summary")
    private String aiSummary;

    @TableField("suggestions")
    private String suggestions;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
