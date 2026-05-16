package com.medical.system;

import com.medical.system.model.entity.*;
import com.medical.system.model.enums.PaymentType;
import com.medical.system.model.enums.Role;
import com.medical.system.model.enums.Specialty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public class TestDataFactory {

    public static User user(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPassword("{noop}password");
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }

    public static Doctor doctor(String identifier, String fullName, Specialty specialty, boolean gp, User user) {
        Doctor doctor = new Doctor();
        doctor.setUniqueIdentifier(identifier);
        doctor.setFullName(fullName);
        doctor.setSpecialty(specialty);
        doctor.setGeneralPractitioner(gp);
        doctor.setUser(user);
        return doctor;
    }

    public static Patient patient(String fullName, String egn, Doctor generalPractitioner, User user) {
        Patient patient = new Patient();
        patient.setFullName(fullName);
        patient.setEgn(egn);
        patient.setGeneralPractitioner(generalPractitioner);
        patient.setUser(user);
        return patient;
    }

    public static Diagnosis diagnosis(String code, String name) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setCode(code);
        diagnosis.setName(name);
        diagnosis.setDescription(name + " description");
        return diagnosis;
    }

    public static HealthInsuranceRecord insuranceRecord(
            Patient patient,
            YearMonth month,
            boolean insured
    ) {
        HealthInsuranceRecord record = new HealthInsuranceRecord();
        record.setPatient(patient);
        record.setMonth(month);
        record.setInsured(insured);
        return record;
    }

    public static Examination examination(
            LocalDate date,
            Doctor doctor,
            Patient patient,
            Diagnosis diagnosis,
            PaymentType paymentType
    ) {
        Examination examination = new Examination();
        examination.setExaminationDate(date);
        examination.setDoctor(doctor);
        examination.setPatient(patient);
        examination.setDiagnosis(diagnosis);
        examination.setPrescribedTreatment("Treatment");
        examination.setPrice(BigDecimal.valueOf(80));
        examination.setPaymentType(paymentType);
        return examination;
    }

    public static SickLeave sickLeave(Examination examination) {
        SickLeave sickLeave = new SickLeave();
        sickLeave.setExamination(examination);
        sickLeave.setStartDate(examination.getExaminationDate().plusDays(1));
        sickLeave.setNumberOfDays(5);
        return sickLeave;
    }
}