package com.medical.system.mapper;


import com.medical.system.dto.sickleave.SickLeaveRequest;
import com.medical.system.dto.sickleave.SickLeaveResponse;
import com.medical.system.model.entity.Examination;
import com.medical.system.model.entity.SickLeave;
import org.springframework.stereotype.Component;

@Component
public class SickLeaveMapper {

    public SickLeaveResponse toResponse(SickLeave sickLeave) {
        if (sickLeave == null) {
            return null;
        }

        return new SickLeaveResponse(
                sickLeave.getId(),
                sickLeave.getExamination() != null ? sickLeave.getExamination().getId() : null,
                sickLeave.getStartDate(),
                sickLeave.getNumberOfDays()
        );
    }

    public SickLeave toEntity(SickLeaveRequest request, Examination examination) {
        SickLeave sickLeave = new SickLeave();

        sickLeave.setExamination(examination);
        sickLeave.setStartDate(request.startDate());
        sickLeave.setNumberOfDays(request.numberOfDays());

        return sickLeave;
    }

    public void updateEntity(SickLeave sickLeave, SickLeaveRequest request, Examination examination) {
        sickLeave.setExamination(examination);
        sickLeave.setStartDate(request.startDate());
        sickLeave.setNumberOfDays(request.numberOfDays());
    }
}