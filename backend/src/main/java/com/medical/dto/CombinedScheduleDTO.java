package com.medical.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 总计划日程 DTO — 按天分组，每天按时间段展示药品
 */
@Data
public class CombinedScheduleDTO {
    /** 第N天 */
    private Integer dayNumber;
    /** 日期 */
    private LocalDate date;
    /** 各时间段的药品列表 */
    private List<PeriodMedicines> periods;

    @Data
    public static class PeriodMedicines {
        /** 时间段：上午 / 中午 / 下午 / 晚上 */
        private String period;
        /** 该时间段需服用的药品 */
        private List<MedicineInfo> medicines;
    }

    @Data
    public static class MedicineInfo {
        private String medicineName;
        private String dosage;
        private Integer status;
        private Long takeId;
    }
}
