package copel.sesproductpackage.line.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amazonaws.regions.Regions;

class SesInfoRegisterRequestSqsEntityTest {
    
    private SesInfoRegisterRequestSqsEntity entity;

    @BeforeEach
    void setUp() {
        entity = new SesInfoRegisterRequestSqsEntity(Regions.AP_NORTHEAST_1, "https://sqs.example.com/queue");
    }

    @Test
    void testConstructor() {
        assertNotNull(entity);
    }

    @Test
    void testGettersAndSetters() {
        entity.setRequestType("message");
        assertEquals("message", entity.getRequestType());

        entity.setFromGroup("groupA");
        assertEquals("groupA", entity.getFromGroup());

        entity.setFromId("user123");
        assertEquals("user123", entity.getFromId());

        entity.setFromName("Taro");
        assertEquals("Taro", entity.getFromName());

        entity.setRawContent("Hello, world!");
        assertEquals("Hello, world!", entity.getRawContent());

        entity.setFileId("file-456");
        assertEquals("file-456", entity.getFileId());

        entity.setFileName("document.pdf");
        assertEquals("document.pdf", entity.getFileName());
    }

    @Test
    void testGetMessageBody() {
        entity.setRequestType("message");
        entity.setFromGroup("groupA");
        entity.setFromId("user123");
        entity.setFromName("Taro");
        entity.setRawContent("Hello, world!");
        entity.setFileId("file-456");
        entity.setFileName("document.pdf");

        String expectedJson = "{"
                + "\"request_type\": \"message\""
                + "\"from_group\": \"groupA\""
                + "\"from_id\": \"user123\""
                + "\"from_name\": \"Taro\""
                + "\"raw_content\": \"Hello, world!\""
                + "\"file_id\": \"file-456\""
                + "\"file_name\": \"document.pdf\""
                + "}";

        assertEquals(expectedJson, entity.getMessageBody());
    }
}
