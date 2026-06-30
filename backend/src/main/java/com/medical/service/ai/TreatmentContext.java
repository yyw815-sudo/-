package com.medical.service.ai;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class TreatmentContext {
    private List<MedicineItem> medicines;
    private List<String> advices;
    private List<String> followUps;
    private String rawTreatment;

    @Data
    public static class MedicineItem {
        private String name;
        private String dose;
        private String frequency;
        private String route;
        private String remark;
    }

    public TreatmentContext() {
        this.medicines = new ArrayList<>();
        this.advices = new ArrayList<>();
        this.followUps = new ArrayList<>();
    }
}
