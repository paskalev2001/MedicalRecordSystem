package com.medical.system.mapper;


import com.medical.system.dto.examination.ExaminationRequest;
import com.medical.system.dto.examination.ExaminationResponse;
import com.medical.system.dto.examination.ExaminationSummaryResponse;
import com.medical.system.model.entity.Diagnosis;
import com.medical.system.model.entity.Doctor;
import com.medical.system.model.entity.Examination;
import com.medical.system.model.entity.Patient;
import com.medical.system.model.enums.PaymentType;
import org.springframework.stereotype.Component;

@Component
public class ExaminationMapper {

    private final DoctorMapper doctorMapper;
    private final PatientMapper patientMapper;
    private final DiagnosisMapper diagnosisMapper;
    private final SickLeaveMapper sickLeaveMapper;

    public ExaminationMapper(
            DoctorMapper doctorMapper,
            PatientMapper patientMapper,
            DiagnosisMapper diagnosisMapper,
            SickLeaveMapper sickLeaveMapper
    ) {
        this.doctorMapper = doctorMapper;
        this.patientMapper = patientMapper;
        this.diagnosisMapper = diagnosisMapper;
        this.sickLeaveMapper = sickLeaveMapper;
    }

    public ExaminationResponse toResponse(Examination examination) {
        if (examination == null) {
            return null;
        }

        return new ExaminationResponse(
                examination.getId(),
                examination.getExaminationDate(),
                doctorMapper.toSummary(examination.getDoctor()),
                patientMapper.toSummary(examination.getPatient()),
                diagnosisMapper.toSummary(examination.getDiagnosis()),
                examination.getPrescribedTreatment(),
                examination.getPrice(),
                examination.getPaymentType(),
                sickLeaveMapper.toResponse(examination.getSickLeave())
        );
    }

    public ExaminationSummaryResponse toSummary(Examination examination) {
        if (examination == null) {
            return null;
        }

        return new ExaminationSummaryResponse(
                examination.getId(),
                examination.getExaminationDate(),
                examination.getDoctor() != null ? examination.getDoctor().getFullName() : null,
                examination.getPatient() != null ? examination.getPatient().getFullName() : null,
                examination.getDiagnosis() != null ? examination.getDiagnosis().getName() : null,
                examination.getPrice(),
                examination.getPaymentType()
        );
    }

    public Examination toEntity(
            ExaminationRequest request,
            Doctor doctor,
            Patient patient,
            Diagnosis diagnosis,
            PaymentType paymentType
    ) {
        Examination examination = new Examination();

        examination.setExaminationDate(request.examinationDate());
        examination.setDoctor(doctor);
        examination.setPatient(patient);
        examination.setDiagnosis(diagnosis);
        examination.setPrescribedTreatment(request.prescribedTreatment());
        examination.setPrice(request.price());
        examination.setPaymentType(paymentType);

        return examination;
    }

    public void updateEntity(
            Examination examination,
            ExaminationRequest request,
            Doctor doctor,
            Patient patient,
            Diagnosis diagnosis,
            PaymentType paymentType
    ) {
        examination.setExaminationDate(request.examinationDate());
        examination.setDoctor(doctor);
        examination.setPatient(patient);
        examination.setDiagnosis(diagnosis);
        examination.setPrescribedTreatment(request.prescribedTreatment());
        examination.setPrice(request.price());
        examination.setPaymentType(paymentType);
    }
}