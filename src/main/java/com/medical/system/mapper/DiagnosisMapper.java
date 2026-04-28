package com.medical.system.mapper;


import com.medical.system.dto.diagnosis.DiagnosisRequest;
import com.medical.system.dto.diagnosis.DiagnosisResponse;
import com.medical.system.dto.diagnosis.DiagnosisSummaryResponse;
import com.medical.system.model.entity.Diagnosis;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisMapper {

    public DiagnosisResponse toResponse(Diagnosis diagnosis) {
        if (diagnosis == null) {
            return null;
        }

        return new DiagnosisResponse(
                diagnosis.getId(),
                diagnosis.getCode(),
                diagnosis.getName(),
                diagnosis.getDescription()
        );
    }

    public DiagnosisSummaryResponse toSummary(Diagnosis diagnosis) {
        if (diagnosis == null) {
            return null;
        }

        return new DiagnosisSummaryResponse(
                diagnosis.getId(),
                diagnosis.getCode(),
                diagnosis.getName()
        );
    }

    public Diagnosis toEntity(DiagnosisRequest request) {
        Diagnosis diagnosis = new Diagnosis();

        diagnosis.setCode(request.code());
        diagnosis.setName(request.name());
        diagnosis.setDescription(request.description());

        return diagnosis;
    }

    public void updateEntity(Diagnosis diagnosis, DiagnosisRequest request) {
        diagnosis.setCode(request.code());
        diagnosis.setName(request.name());
        diagnosis.setDescription(request.description());
    }
}