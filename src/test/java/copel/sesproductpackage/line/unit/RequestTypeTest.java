package copel.sesproductpackage.line.unit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequestTypeTest {

    @Test
    void testGetEnum() {
        // 各コードに対応するEnumが正しく返るか
        assertEquals(RequestType.LineMessage, RequestType.getEnum("11"));
        assertEquals(RequestType.LineFile, RequestType.getEnum("12"));
        assertEquals(RequestType.GmailMessage, RequestType.getEnum("21"));
        assertEquals(RequestType.GmailFile, RequestType.getEnum("22"));
        assertEquals(RequestType.OtherMessage, RequestType.getEnum("01"));
        assertEquals(RequestType.OtherFile, RequestType.getEnum("02"));

        // 不正なコードが OtherMessage になるか
        assertEquals(RequestType.OtherMessage, RequestType.getEnum("99"));
        assertEquals(RequestType.OtherMessage, RequestType.getEnum(""));

        // null の場合は null を返すか
        assertNull(RequestType.getEnum(null));
    }

    @Test
    void testGetCode() {
        // 各Enumの getCode() が正しいか
        assertEquals("11", RequestType.LineMessage.getCode());
        assertEquals("12", RequestType.LineFile.getCode());
        assertEquals("21", RequestType.GmailMessage.getCode());
        assertEquals("22", RequestType.GmailFile.getCode());
        assertEquals("01", RequestType.OtherMessage.getCode());
        assertEquals("02", RequestType.OtherFile.getCode());
    }
}
