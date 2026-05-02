package com.medical.system.controller;

import com.medical.system.dto.examination.ExaminationResponse;
import com.medical.system.dto.patient.PatientResponse;
import com.medical.system.dto.patient.PatientSummaryResponse;
import com.medical.system.dto.report.*;
import com.medical.system.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/patients/by-diagnosis/{diagnosisId}")
    public ResponseEntity<List<PatientSummaryResponse>> getPatientsByDiagnosis(
            @PathVariable Long diagnosisId
    ) {
        return ResponseEntity.ok(reportService.getPatientsByDiagnosis(diagnosisId));
    }

    @GetMapping("/diagnoses/most-common")
    public ResponseEntity<DiagnosisCountResponse> getMostCommonDiagnosis() {
        return ResponseEntity.ok(reportService.getMostCommonDiagnosis());
    }

    @GetMapping("/patients/by-general-practitioner/{doctorId}")
    public ResponseEntity<List<PatientResponse>> getPatientsByGeneralPractitioner(
            @PathVariable Long doctorId
    ) {
        return ResponseEntity.ok(reportService.getPatientsByGeneralPractitioner(doctorId));
    }

    @GetMapping("/payments/patient-total")
    public ResponseEntity<BigDecimal> getTotalPaidByPatients() {
        return ResponseEntity.ok(reportService.getTotalPaidByPatients());
    }

    @GetMapping("/payments/patient-total/by-doctor")
    public ResponseEntity<List<DoctorRevenueResponse>> getPaidByPatientsGroupedByDoctor() {
        return ResponseEntity.ok(reportService.getPaidByPatientsGroupedByDoctor());
    }

    @GetMapping("/general-practitioners/patient-count")
    public ResponseEntity<List<DoctorPatientCountResponse>> getPatientCountsByGeneralPractitioner() {
        return ResponseEntity.ok(reportService.getPatientCountsByGeneralPractitioner());
    }

    @GetMapping("/doctors/visit-count")
    public ResponseEntity<List<DoctorVisitCountResponse>> getVisitCountsByDoctor() {
        return ResponseEntity.ok(reportService.getVisitCountsByDoctor());
    }

    @GetMapping("/patients/{patientId}/visit-history")
    public ResponseEntity<List<ExaminationResponse>> getPatientVisitHistory(
            @PathVariable Long patientId
    ) {
        return ResponseEntity.ok(reportService.getPatientVisitHistory(patientId));
    }

    @GetMapping("/examinations")
    public ResponseEntity<List<ExaminationResponse>> getExaminationsByDoctorAndOrPeriod(
            @RequestParam(required = false) Long doctorId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return ResponseEntity.ok(
                reportService.getExaminationsByDoctorAndOrPeriod(
                        doctorId,
                        startDate,
                        endDate
                )
        );
    }

    @GetMapping("/sick-leaves/month-with-most")
    public ResponseEntity<SickLeaveMonthCountResponse> getMonthWithMostSickLeaves() {
        return ResponseEntity.ok(reportService.getMonthWithMostSickLeaves());
    }

    @GetMapping("/sick-leaves/doctors-with-most")
    public ResponseEntity<List<DoctorSickLeaveCountResponse>> getDoctorsWithMostSickLeaves() {
        return ResponseEntity.ok(reportService.getDoctorsWithMostSickLeaves());
    }
}