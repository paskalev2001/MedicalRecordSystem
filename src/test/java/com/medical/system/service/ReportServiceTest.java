package com.medical.system.service;

import com.medical.system.TestDataFactory;
import com.medical.system.dto.report.*;
import com.medical.system.model.entity.*;
import com.medical.system.model.enums.PaymentType;
import com.medical.system.model.enums.Role;
import com.medical.system.model.enums.Specialty;
import com.medical.system.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    private ExaminationRepository examinationRepository;

    @Autowired
    private SickLeaveRepository sickLeaveRepository;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnMostCommonDiagnosis() {
        ReportData data = createReportData("MOST-COMMON");

        examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 1),
                        data.doctor1(),
                        data.patient1(),
                        data.diagnosis1(),
                        PaymentType.PATIENT
                )
        );

        examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        data.doctor1(),
                        data.patient2(),
                        data.diagnosis1(),
                        PaymentType.PATIENT
                )
        );

        examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 3),
                        data.doctor2(),
                        data.patient1(),
                        data.diagnosis2(),
                        PaymentType.NHIF
                )
        );

        DiagnosisCountResponse result = reportService.getMostCommonDiagnosis();

        assertThat(result).isNotNull();
        assertThat(result.diagnosisName()).isEqualTo(data.diagnosis1().getName());
        assertThat(result.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnPatientsByDiagnosis() {
        ReportData data = createReportData("PATIENTS-DIAG");

        examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 1),
                        data.doctor1(),
                        data.patient1(),
                        data.diagnosis1(),
                        PaymentType.PATIENT
                )
        );

        examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        data.doctor2(),
                        data.patient2(),
                        data.diagnosis1(),
                        PaymentType.NHIF
                )
        );

        var result = reportService.getPatientsByDiagnosis(data.diagnosis1().getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("fullName")
                .containsExactlyInAnyOrder(
                        data.patient1().getFullName(),
                        data.patient2().getFullName()
                );
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnTotalPaidByPatients() {
        ReportData data = createReportData("TOTAL-PAID");

        Examination examination1 = TestDataFactory.examination(
                LocalDate.of(2026, 5, 1),
                data.doctor1(),
                data.patient1(),
                data.diagnosis1(),
                PaymentType.PATIENT
        );
        examination1.setPrice(BigDecimal.valueOf(80));

        Examination examination2 = TestDataFactory.examination(
                LocalDate.of(2026, 5, 2),
                data.doctor2(),
                data.patient2(),
                data.diagnosis2(),
                PaymentType.PATIENT
        );
        examination2.setPrice(BigDecimal.valueOf(60));

        Examination examination3 = TestDataFactory.examination(
                LocalDate.of(2026, 5, 3),
                data.doctor1(),
                data.patient2(),
                data.diagnosis1(),
                PaymentType.NHIF
        );
        examination3.setPrice(BigDecimal.valueOf(100));

        examinationRepository.save(examination1);
        examinationRepository.save(examination2);
        examinationRepository.save(examination3);

        BigDecimal result = reportService.getTotalPaidByPatients();

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(140));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnPaidByPatientsGroupedByDoctor() {
        ReportData data = createReportData("PAID-BY-DOCTOR");

        Examination examination1 = TestDataFactory.examination(
                LocalDate.of(2026, 5, 1),
                data.doctor1(),
                data.patient1(),
                data.diagnosis1(),
                PaymentType.PATIENT
        );
        examination1.setPrice(BigDecimal.valueOf(80));

        Examination examination2 = TestDataFactory.examination(
                LocalDate.of(2026, 5, 2),
                data.doctor1(),
                data.patient2(),
                data.diagnosis2(),
                PaymentType.PATIENT
        );
        examination2.setPrice(BigDecimal.valueOf(60));

        Examination examination3 = TestDataFactory.examination(
                LocalDate.of(2026, 5, 3),
                data.doctor2(),
                data.patient1(),
                data.diagnosis1(),
                PaymentType.PATIENT
        );
        examination3.setPrice(BigDecimal.valueOf(50));

        examinationRepository.save(examination1);
        examinationRepository.save(examination2);
        examinationRepository.save(examination3);

        List<DoctorRevenueResponse> result =
                reportService.getPaidByPatientsGroupedByDoctor();

        assertThat(result).hasSize(2);

        DoctorRevenueResponse doctor1Revenue = result.stream()
                .filter(item -> item.doctorId().equals(data.doctor1().getId()))
                .findFirst()
                .orElseThrow();

        assertThat(doctor1Revenue.totalPaidByPatients())
                .isEqualByComparingTo(BigDecimal.valueOf(140));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnVisitCountsByDoctor() {
        ReportData data = createReportData("VISIT-COUNT");

        examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 1),
                        data.doctor1(),
                        data.patient1(),
                        data.diagnosis1(),
                        PaymentType.PATIENT
                )
        );

        examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        data.doctor1(),
                        data.patient2(),
                        data.diagnosis2(),
                        PaymentType.NHIF
                )
        );

        examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 3),
                        data.doctor2(),
                        data.patient1(),
                        data.diagnosis1(),
                        PaymentType.PATIENT
                )
        );

        List<DoctorVisitCountResponse> result = reportService.getVisitCountsByDoctor();

        DoctorVisitCountResponse doctor1Count = result.stream()
                .filter(item -> item.doctorId().equals(data.doctor1().getId()))
                .findFirst()
                .orElseThrow();

        assertThat(doctor1Count.visitCount()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnPatientCountsByGeneralPractitioner() {
        ReportData data = createReportData("GP-PATIENT-COUNT");

        List<DoctorPatientCountResponse> result =
                reportService.getPatientCountsByGeneralPractitioner();

        DoctorPatientCountResponse gpCount = result.stream()
                .filter(item -> item.doctorId().equals(data.gp().getId()))
                .findFirst()
                .orElseThrow();

        assertThat(gpCount.patientCount()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnMonthWithMostSickLeaves() {
        ReportData data = createReportData("SICK-MONTH");

        Examination examination1 = examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 1),
                        data.doctor1(),
                        data.patient1(),
                        data.diagnosis1(),
                        PaymentType.PATIENT
                )
        );

        Examination examination2 = examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        data.doctor1(),
                        data.patient2(),
                        data.diagnosis2(),
                        PaymentType.NHIF
                )
        );

        SickLeave sickLeave1 = TestDataFactory.sickLeave(examination1);
        sickLeave1.setStartDate(LocalDate.of(2026, 5, 3));

        SickLeave sickLeave2 = TestDataFactory.sickLeave(examination2);
        sickLeave2.setStartDate(LocalDate.of(2026, 5, 4));

        sickLeaveRepository.save(sickLeave1);
        sickLeaveRepository.save(sickLeave2);

        SickLeaveMonthCountResponse result = reportService.getMonthWithMostSickLeaves();

        assertThat(result).isNotNull();
        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.month()).isEqualTo(5);
        assertThat(result.sickLeaveCount()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnDoctorsWithMostSickLeaves() {
        ReportData data = createReportData("SICK-DOCTOR");

        Examination examination1 = examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 1),
                        data.doctor1(),
                        data.patient1(),
                        data.diagnosis1(),
                        PaymentType.PATIENT
                )
        );

        Examination examination2 = examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        data.doctor1(),
                        data.patient2(),
                        data.diagnosis2(),
                        PaymentType.NHIF
                )
        );

        Examination examination3 = examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 3),
                        data.doctor2(),
                        data.patient1(),
                        data.diagnosis1(),
                        PaymentType.PATIENT
                )
        );

        sickLeaveRepository.save(TestDataFactory.sickLeave(examination1));
        sickLeaveRepository.save(TestDataFactory.sickLeave(examination2));
        sickLeaveRepository.save(TestDataFactory.sickLeave(examination3));

        List<DoctorSickLeaveCountResponse> result =
                reportService.getDoctorsWithMostSickLeaves();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).doctorId()).isEqualTo(data.doctor1().getId());
        assertThat(result.get(0).sickLeaveCount()).isEqualTo(2);
    }

    private ReportData createReportData(String suffix) {
        User gpUser = userRepository.save(
                TestDataFactory.user("gp_report_" + suffix, Role.DOCTOR)
        );

        User doctorUser1 = userRepository.save(
                TestDataFactory.user("doctor1_report_" + suffix, Role.DOCTOR)
        );

        User doctorUser2 = userRepository.save(
                TestDataFactory.user("doctor2_report_" + suffix, Role.DOCTOR)
        );

        User patientUser1 = userRepository.save(
                TestDataFactory.user("patient1_report_" + suffix, Role.PATIENT)
        );

        User patientUser2 = userRepository.save(
                TestDataFactory.user("patient2_report_" + suffix, Role.PATIENT)
        );

        Doctor gp = doctorRepository.save(
                TestDataFactory.doctor(
                        "GP-REPORT-" + suffix,
                        "Dr. GP Report " + suffix,
                        Specialty.GENERAL_PRACTITIONER,
                        true,
                        gpUser
                )
        );

        Doctor doctor1 = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC1-REPORT-" + suffix,
                        "Dr. Report One " + suffix,
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser1
                )
        );

        Doctor doctor2 = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC2-REPORT-" + suffix,
                        "Dr. Report Two " + suffix,
                        Specialty.NEUROLOGY,
                        false,
                        doctorUser2
                )
        );

        Patient patient1 = patientRepository.save(
                TestDataFactory.patient(
                        "Report Patient One " + suffix,
                        generateEgn("1" + suffix),
                        gp,
                        patientUser1
                )
        );

        Patient patient2 = patientRepository.save(
                TestDataFactory.patient(
                        "Report Patient Two " + suffix,
                        generateEgn("2" + suffix),
                        gp,
                        patientUser2
                )
        );

        Diagnosis diagnosis1 = diagnosisRepository.save(
                TestDataFactory.diagnosis(
                        "R1-" + suffix,
                        "Report Diagnosis One " + suffix
                )
        );

        Diagnosis diagnosis2 = diagnosisRepository.save(
                TestDataFactory.diagnosis(
                        "R2-" + suffix,
                        "Report Diagnosis Two " + suffix
                )
        );

        return new ReportData(
                gp,
                doctor1,
                doctor2,
                patient1,
                patient2,
                diagnosis1,
                diagnosis2
        );
    }

    private String generateEgn(String value) {
        int number = Math.abs(value.hashCode() % 1_000_000);
        return "95" + String.format("%08d", number).substring(0, 8);
    }

    private record ReportData(
            Doctor gp,
            Doctor doctor1,
            Doctor doctor2,
            Patient patient1,
            Patient patient2,
            Diagnosis diagnosis1,
            Diagnosis diagnosis2
    ) {
    }
}