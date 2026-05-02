package com.medical.system.service;

import com.medical.system.dto.examination.ExaminationResponse;
import com.medical.system.dto.patient.PatientResponse;
import com.medical.system.dto.patient.PatientSummaryResponse;
import com.medical.system.dto.report.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    List<PatientSummaryResponse> getPatientsByDiagnosis(Long diagnosisId);

    DiagnosisCountResponse getMostCommonDiagnosis();

    List<PatientResponse> getPatientsByGeneralPractitioner(Long doctorId);

    BigDecimal getTotalPaidByPatients();

    List<DoctorRevenueResponse> getPaidByPatientsGroupedByDoctor();

    List<DoctorPatientCountResponse> getPatientCountsByGeneralPractitioner();

    List<DoctorVisitCountResponse> getVisitCountsByDoctor();

    List<ExaminationResponse> getPatientVisitHistory(Long patientId);

    List<ExaminationResponse> getExaminationsByDoctorAndOrPeriod(
            Long doctorId,
            LocalDate startDate,
            LocalDate endDate
    );

    SickLeaveMonthCountResponse getMonthWithMostSickLeaves();

    List<DoctorSickLeaveCountResponse> getDoctorsWithMostSickLeaves();
}