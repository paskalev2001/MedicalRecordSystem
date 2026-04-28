package com.medical.system.mapper;


import com.medical.system.dto.doctor.DoctorRequest;
import com.medical.system.dto.doctor.DoctorResponse;
import com.medical.system.dto.doctor.DoctorSummaryResponse;
import com.medical.system.model.entity.Doctor;
import com.medical.system.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public DoctorResponse toResponse(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        return new DoctorResponse(
                doctor.getId(),
                doctor.getUniqueIdentifier(),
                doctor.getFullName(),
                doctor.getSpecialty(),
                doctor.isGeneralPractitioner(),
                doctor.getUser() != null ? doctor.getUser().getId() : null
        );
    }

    public DoctorSummaryResponse toSummary(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        return new DoctorSummaryResponse(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getSpecialty(),
                doctor.isGeneralPractitioner()
        );
    }

    public Doctor toEntity(DoctorRequest request, User user) {
        Doctor doctor = new Doctor();

        doctor.setUniqueIdentifier(request.uniqueIdentifier());
        doctor.setFullName(request.fullName());
        doctor.setSpecialty(request.specialty());
        doctor.setGeneralPractitioner(request.generalPractitioner());
        doctor.setUser(user);

        return doctor;
    }

    public void updateEntity(Doctor doctor, DoctorRequest request, User user) {
        doctor.setUniqueIdentifier(request.uniqueIdentifier());
        doctor.setFullName(request.fullName());
        doctor.setSpecialty(request.specialty());
        doctor.setGeneralPractitioner(request.generalPractitioner());
        doctor.setUser(user);
    }
}
