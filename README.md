# Electronic Medical Record System

Уеб приложение за поддържане на електронно медицинско досие на пациент.

Проектът реализира RESTful backend със Spring Boot, база данни, role-based security, справки и React + Vite потребителски интерфейс.

Потребителският интерфейс е достъпен в това repository:
https://github.com/paskalev2001/medical-system-ui

## 1. Основни функционалности

Системата поддържа управление на:

* Потребители и роли
* Лекари
* Пациенти
* Диагнози
* Здравноосигурителни записи
* Прегледи
* Болнични листове
* Справки и статистики

## 2. Роли и права

### ADMIN

Администраторът има пълен достъп до всички данни и може да:

* създава, редактира и изтрива потребители;
* създава, редактира и изтрива лекари;
* създава, редактира и изтрива пациенти;
* създава, редактира и изтрива диагнози;
* управлява здравноосигурителни записи;
* създава, редактира и изтрива прегледи;
* създава, редактира и изтрива болнични листове;
* вижда всички справки, включително финансовите.

### DOCTOR

Лекарят може да:

* вижда медицинските данни на пациентите;
* създава прегледи само от свое име;
* редактира и изтрива само прегледите, които сам е извършил;
* създава и редактира болнични към свои прегледи;
* вижда справки без финансовите административни справки.

### PATIENT

Пациентът може да:

* вижда собствените си прегледи;
* вижда собствените си болнични листове;
* вижда собствените си здравноосигурителни записи;
* няма достъп до чужди медицински данни.

## 3. Технологии

### Backend

* Java 17+
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* Bean Validation
* H2 / SQL database
* JUnit 5
* MockMvc
* Spring Security Test

### Frontend

* React
* Vite
* React Router
* Axios
* CSS

## 4. Стартиране на backend

От root директорията на backend проекта:

```powershell
.\gradlew bootRun
```

Backend приложението се стартира на:
```text
http://localhost:8080
```

## 5. Стартиране на frontend

От директорията `client`:

```bash
npm install
npm run dev
```

Frontend приложението се стартира на:

```text
http://localhost:5173
```


## 6. Тестови акаунти

Минималният admin user се създава автоматично чрез `DataInitializer`.

```text
Username: admin
Password: admin123
Role: ADMIN
```

Останалите потребители могат да се създават през:

```text
Admin Users screen
```

или чрез endpoint:

```http
POST /api/admin/users
```

## 7. Основни REST endpoints

### Auth

```http
POST /api/auth/register
GET  /api/auth/me
```

### Admin Users

```http
POST   /api/admin/users
GET    /api/admin/users
GET    /api/admin/users/{id}
PUT    /api/admin/users/{id}
PATCH  /api/admin/users/{id}/password
DELETE /api/admin/users/{id}
```

### Doctors

```http
POST   /api/doctors
GET    /api/doctors
GET    /api/doctors/{id}
GET    /api/doctors/general-practitioners
PUT    /api/doctors/{id}
DELETE /api/doctors/{id}
```

### Patients

```http
POST   /api/patients
GET    /api/patients
GET    /api/patients/{id}
GET    /api/patients/me
PUT    /api/patients/{id}
DELETE /api/patients/{id}
```

### Diagnoses

```http
POST   /api/diagnoses
GET    /api/diagnoses
GET    /api/diagnoses/{id}
PUT    /api/diagnoses/{id}
DELETE /api/diagnoses/{id}
```

### Health Insurance Records

```http
POST   /api/health-insurance-records
GET    /api/health-insurance-records
GET    /api/health-insurance-records/{id}
GET    /api/health-insurance-records/me
PUT    /api/health-insurance-records/{id}
DELETE /api/health-insurance-records/{id}
```

### Examinations

```http
POST   /api/examinations
GET    /api/examinations
GET    /api/examinations/{id}
GET    /api/examinations/me
PUT    /api/examinations/{id}
DELETE /api/examinations/{id}
```

### Sick Leaves

```http
POST   /api/sick-leaves
GET    /api/sick-leaves
GET    /api/sick-leaves/{id}
GET    /api/sick-leaves/me
PUT    /api/sick-leaves/{id}
DELETE /api/sick-leaves/{id}
```

### Reports

```http
GET /api/reports/patients/by-diagnosis/{diagnosisId}
GET /api/reports/diagnoses/most-common
GET /api/reports/patients/by-general-practitioner/{doctorId}
GET /api/reports/payments/patient-total
GET /api/reports/payments/patient-total/by-doctor
GET /api/reports/general-practitioners/patient-count
GET /api/reports/doctors/visit-count
GET /api/reports/patients/{patientId}/visit-history
GET /api/reports/examinations
GET /api/reports/sick-leaves/month-with-most
GET /api/reports/sick-leaves/doctors-with-most
```

## 8. Бизнес правила

### Плащане на преглед

При създаване на преглед системата проверява здравноосигурителния статус на пациента за последните 6 месеца спрямо датата на прегледа.

* Ако пациентът има 6 осигурени месеца, прегледът се заплаща от НЗОК(NHIF).
* Ако пациентът няма пълни здравни осигуровки, прегледът се заплаща от пациента.

Стойността се записва в `paymentType`:

```text
NHIF
PATIENT
```

### Delete protection

Системата не позволява изтриване на:

* лекар, ако има извършени прегледи;
* лекар, ако е личен лекар на пациенти;
* пациент, ако има прегледи;
* диагноза, ако се използва в преглед;
* преглед, ако към него има издаден болничен;
* user, ако е свързан с doctor или patient profile.

## 9. Автоматизирани тестове

Проектът съдържа тестове за:

* business logic;
* role-based security;
* DTO validation;
* auth/user management;
* delete protection;
* reports/statistics;
* CRUD controller flows.

Пускане на всички тестове:

```powershell
.\gradlew clean test
```

Пускане на конкретен тестов клас:

```powershell
.\gradlew test --tests "*ExaminationControllerTest"
```

HTML test report:

```text
build/reports/tests/test/index.html
```

## 10. Frontend екрани

Frontend приложението съдържа:

* Login
* Dashboard
* Admin Users
* Doctors
* Patients
* Diagnoses
* Health Insurance Records
* Examinations
* Sick Leaves
* Reports
* Patient-specific screens

## 11. Примерен демонстрационен flow

1. Login като admin.
2. Създаване на DOCTOR user.
3. Създаване на PATIENT user.
4. Създаване на GP doctor profile.
5. Създаване на specialist doctor profile.
6. Създаване на patient profile.
7. Създаване на diagnosis.
8. Добавяне на 6 здравноосигурителни месеца.
9. Създаване на examination.
10. Проверка дали `paymentType = NHIF`.
11. Създаване на sick leave.
12. Преглед на reports.
13. Login като patient.
14. Проверка на собствените прегледи, болнични и осигуровки.

## 12. Статус на проекта

Проектът покрива следните функционални и технологични изисквания:

* RESTful backend;
* база данни;
* CRUD операции;
* DTO и mapper layer;
* service layer;
* controller layer;
* Spring Security;
* роли и права;
* exception handling;
* validation;
* справки и статистики;
* React frontend;
* автоматизирани тестове;
* Swagger/OpenAPI документация.
