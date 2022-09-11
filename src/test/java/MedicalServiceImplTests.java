import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
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

public class MedicalServiceImplTests {
    private static SendAlertService alertService;
    private static MedicalService medicalService;
    private static ArgumentCaptor<String> argumentCaptor;
    private static final String patientId = "1";
    private static final String patientNeedHelpInfo = String.format("Warning, patient with id: %s, need help", patientId);

    @BeforeEach
    void init() {
        PatientInfoFileRepository repository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito.when(repository.getById(Mockito.anyString()))
                .thenReturn(new PatientInfo(
                        patientId,
                        "Иван",
                        "Иванов",
                        LocalDate.of(1980, 1, 1),
                        new HealthInfo(new BigDecimal("36.6"), new BloodPressure(120,80))
                        ));
        alertService = Mockito.mock(SendAlertService.class);
        medicalService = new MedicalServiceImpl(repository, alertService);
        argumentCaptor = ArgumentCaptor.forClass(String.class);
    }

    public static Stream<Arguments> testCheckBloodPressureArgs() {
        return Stream.of(
                Arguments.of(new BloodPressure(110,70), patientNeedHelpInfo),
                Arguments.of(new BloodPressure(120,80), ""),
                Arguments.of(new BloodPressure(140,80), patientNeedHelpInfo),
                Arguments.of(new BloodPressure(150,100), patientNeedHelpInfo)
        );
    }

    @ParameterizedTest
    @MethodSource("testCheckBloodPressureArgs")
    void testCheckBloodPressure(BloodPressure bloodPressure, String expected) {
        //act
        medicalService.checkBloodPressure(patientId, bloodPressure);

        //assert
        if (expected.equals("")) {
            Mockito.verify(alertService, Mockito.times(0)).send(Mockito.anyString());
        } else {
            Mockito.verify(alertService, Mockito.times(1)).send(argumentCaptor.capture());
            Assertions.assertEquals(expected, argumentCaptor.getValue());
        }
    }

    public static Stream<Arguments> testCheckTemperatureArgs() {
        return Stream.of(
                Arguments.of(new BigDecimal("34.3"), patientNeedHelpInfo),
                Arguments.of(new BigDecimal("35.6"), ""),
                Arguments.of(new BigDecimal("36.6"), "")
        );
    }

    @ParameterizedTest
    @MethodSource("testCheckTemperatureArgs")
    void testCheckTemperature(BigDecimal temperature, String expected) {
        //act
        medicalService.checkTemperature(patientId, temperature);

        //assert
        if (expected.equals("")) {
            Mockito.verify(alertService, Mockito.times(0)).send(Mockito.anyString());
        } else {
            Mockito.verify(alertService, Mockito.times(1)).send(argumentCaptor.capture());
            Assertions.assertEquals(expected, argumentCaptor.getValue());
        }
    }
}