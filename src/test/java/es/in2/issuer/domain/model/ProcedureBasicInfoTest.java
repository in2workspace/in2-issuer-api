package es.in2.issuer.domain.model;

import es.in2.issuer.domain.model.dto.ProcedureBasicInfo;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcedureBasicInfoTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        String expectedFullName = "John Doe";
        String expectedStatus = "In Progress";
        Timestamp timestamp = Timestamp.valueOf("2023-01-01 12:00:00");

        // Act
        ProcedureBasicInfo procedureBasicInfo = new ProcedureBasicInfo(
                uuid,
                expectedFullName,
                expectedStatus,
                timestamp
        );

        // Assert
        assertEquals(uuid, procedureBasicInfo.procedureId());
        assertEquals(expectedFullName, procedureBasicInfo.fullName());
        assertEquals(expectedStatus, procedureBasicInfo.status());
        assertEquals(timestamp, procedureBasicInfo.updated());
    }

    @Test
    void testSetters() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        String newFullName = "Jane Doe";
        String newStatus = "Completed";
        Timestamp timestamp = Timestamp.valueOf("2024-01-01 12:00:00");

        // Act
        ProcedureBasicInfo procedureBasicInfo = ProcedureBasicInfo.builder()
                .procedureId(uuid)
                .fullName(newFullName)
                .status(newStatus)
                .updated(timestamp)
                .build();

        // Assert
        assertEquals(uuid, procedureBasicInfo.procedureId());
        assertEquals(newFullName, procedureBasicInfo.fullName());
        assertEquals(newStatus, procedureBasicInfo.status());
        assertEquals(timestamp, procedureBasicInfo.updated());
    }

    @Test
    void lombokGeneratedMethodsTest() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        String expectedFullName = "John Doe";
        String expectedStatus = "In Progress";
        Timestamp timestamp = Timestamp.valueOf("2023-01-01 12:00:00");

        ProcedureBasicInfo procedureBasicInfo1 = new ProcedureBasicInfo(
                uuid,
                expectedFullName,
                expectedStatus,
                timestamp
        );
        ProcedureBasicInfo procedureBasicInfo2 = new ProcedureBasicInfo(
                uuid,
                expectedFullName,
                expectedStatus,
                timestamp
        );

        // Assert
        assertEquals(procedureBasicInfo1, procedureBasicInfo2);
        assertEquals(procedureBasicInfo1.hashCode(), procedureBasicInfo2.hashCode());
    }
}