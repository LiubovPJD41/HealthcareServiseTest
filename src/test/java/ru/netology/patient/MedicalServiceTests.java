package ru.netology.patient;

        import org.junit.jupiter.api.AfterEach;
        import org.junit.jupiter.api.BeforeEach;
        import org.junit.jupiter.api.Test;
        import org.junit.jupiter.params.ParameterizedTest;
        import org.junit.jupiter.params.provider.Arguments;
        import org.junit.jupiter.params.provider.MethodSource;
        import org.mockito.Mockito;
        import ru.netology.patient.entity.BloodPressure;
        import ru.netology.patient.entity.HealthInfo;
        import ru.netology.patient.entity.PatientInfo;
        import ru.netology.patient.repository.PatientInfoFileRepository;
        import ru.netology.patient.service.alert.SendAlertService;
        import ru.netology.patient.service.medical.MedicalService;
        import ru.netology.patient.service.medical.MedicalServiceImpl;

        import java.math.BigDecimal;
        import java.time.LocalDate;
        import java.util.stream.Stream;

public class MedicalServiceTests {

    private PatientInfo mary;
    private SendAlertService alertService;
    private PatientInfoFileRepository patientInfoRepository;

    @BeforeEach
    public void setup() {
        patientInfoRepository = Mockito.mock(PatientInfoFileRepository.class);
        mary = new PatientInfo("Мария", "Сергеева",
                LocalDate.of(1979, 03, 15),
                new HealthInfo(new BigDecimal("36.6"), new BloodPressure(120, 80)));
        alertService = Mockito.mock(SendAlertService.class);
    }

    @AfterEach
    public void teardown() {
        patientInfoRepository = null;
        mary = null;
        alertService = null;
    }

    @ParameterizedTest
    @MethodSource("forBloodPressureCheck")
    public void checkBloodPressureTest(BloodPressure bloodPressure, int numberOfMethodCalls) {
        Mockito.when(patientInfoRepository.getById(Mockito.any()))
                .thenReturn(mary);
        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
        medicalService.checkBloodPressure("id", bloodPressure);
        Mockito.verify(alertService, Mockito.times(numberOfMethodCalls)).send(Mockito.contains("Warning"));
    }

    @ParameterizedTest
    @MethodSource("forCheckTemperature")
    public void checkTemperatureTest(BigDecimal temperature, int numberOfMethodCalls) {
        Mockito.when(patientInfoRepository.getById(Mockito.any()))
                .thenReturn(mary);
        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
        medicalService.checkTemperature("id", temperature);
        Mockito.verify(alertService, Mockito.times(numberOfMethodCalls)).send(Mockito.anyString());
    }

    @Test
    public void checkNormalTemperature() {
        Mockito.when(patientInfoRepository.getById(Mockito.any()))
                .thenReturn(mary);
        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
        medicalService.checkTemperature("id", mary.getHealthInfo().getNormalTemperature());
        Mockito.verify(alertService, Mockito.never()).send(Mockito.anyString());
    }

    @Test
    public void checkNormalBloodPressure() {
        Mockito.when(patientInfoRepository.getById(Mockito.any()))
                .thenReturn(mary);
        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
        medicalService.checkBloodPressure("id", mary.getHealthInfo().getBloodPressure());
        Mockito.verify(alertService, Mockito.never()).send(Mockito.anyString());
    }

    public static Stream<Arguments> forBloodPressureCheck() {
        return Stream.of(
                Arguments.of(new BloodPressure(150, 90), 1),
                Arguments.of(new BloodPressure(110, 70), 1),
                Arguments.of(new BloodPressure(120, 80), 0),
                Arguments.of(new BloodPressure(100, 60), 1),
                Arguments.of(new BloodPressure(120, 70), 1)
        );
    }

    public static Stream<Arguments> forCheckTemperature() {
        return Stream.of(
                Arguments.of(new BigDecimal("38.8"), 0),
                Arguments.of(new BigDecimal("39.1"), 0),
                Arguments.of(new BigDecimal("36.6"), 0),
                Arguments.of(new BigDecimal("35.5"), 0),
                Arguments.of(new BigDecimal("34.0"), 1)
        );
    }


}