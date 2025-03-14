package copel.sesproductpackage.line.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

public class LineMessagingApiWebhookEntityTest {

    private static final String SAMPLE_JSON = """
        {
            "events": [
                {
                    "type": "message",
                    "timestamp": 1613850213000,
                    "source": {
                        "type": "user",
                        "userId": "U1234567890"
                    },
                    "message": {
                        "type": "text",
                        "text": "こんにちは"
                    }
                }
            ]
        }
        """;

    @Test
    void testConstructorAndGetters() {
        LineMessagingApiWebhookEntity entity = new LineMessagingApiWebhookEntity(SAMPLE_JSON);
        
        assertEquals("message", entity.getType());
        assertEquals("user", entity.getSourceType());
        assertEquals("U1234567890", entity.getUserId());
        assertNull(entity.getGroupId());
        assertEquals("text", entity.getMessageType());
        assertEquals("こんにちは", entity.getText());
        assertNull(entity.getFileName());
        assertNull(entity.getFileId());
    }

    @Test
    void testIsValid() {
        LineMessagingApiWebhookEntity entity = new LineMessagingApiWebhookEntity(SAMPLE_JSON);
        assertTrue(entity.isValid());
    }

    @Test
    void testIsMessage() {
        LineMessagingApiWebhookEntity entity = new LineMessagingApiWebhookEntity(SAMPLE_JSON);
        assertTrue(entity.isMessage());
    }

    @Test
    void testIsFile() {
        String fileJson = """
        {
            "events": [
                {
                    "type": "message",
                    "timestamp": 1613850213000,
                    "source": { "type": "user", "userId": "U1234567890" },
                    "message": { "type": "file", "id": "file123", "fileName": "test.txt" }
                }
            ]
        }
        """;
        LineMessagingApiWebhookEntity entity = new LineMessagingApiWebhookEntity(fileJson);
        assertTrue(entity.isFile());
        assertEquals("file123", entity.getFileId());
        assertEquals("test.txt", entity.getFileName());
    }

    @Test
    void testIsGroup() {
        String groupJson = """
        {
            "events": [
                {
                    "type": "message",
                    "timestamp": 1613850213000,
                    "source": { "type": "group", "groupId": "G1234567890" },
                    "message": { "type": "text", "text": "Hello" }
                }
            ]
        }
        """;
        LineMessagingApiWebhookEntity entity = new LineMessagingApiWebhookEntity(groupJson);
        assertTrue(entity.isGroup());
        assertEquals("G1234567890", entity.getGroupId());
    }

    @Test
    void testIsPersonal() {
        LineMessagingApiWebhookEntity entity = new LineMessagingApiWebhookEntity(SAMPLE_JSON);
        assertTrue(entity.isPersonal());
    }

    @Test
    void testGetTimestamp() {
        LineMessagingApiWebhookEntity entity = new LineMessagingApiWebhookEntity(SAMPLE_JSON);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expectedTimestamp = LocalDateTime.of(2021, 2, 20, 19, 43, 33).format(formatter);
        assertEquals(expectedTimestamp, entity.getTimestamp());
    }

    @Test
    void testInvalidJsonThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new LineMessagingApiWebhookEntity("invalid_json"));
    }
}
