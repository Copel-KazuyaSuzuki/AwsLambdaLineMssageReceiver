package copel.sesproductpackage.line.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

class SQSEntityBaseTest {

    private static final String QUEUE_URL = "https://sqs.example.com/test-queue";

    @Mock
    private AmazonSQS mockSqsClient;

    @InjectMocks
    private SesInfoRegisterRequestSqsEntity sqsEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 抽象クラスを匿名クラスで実装
        sqsEntity = new SesInfoRegisterRequestSqsEntity(Regions.AP_NORTHEAST_1, QUEUE_URL) {
            @Override
            protected String getMessageBody() {
                return "{\"message\": \"test\"}";
            }
        };

        // SQSクライアントをモックに置き換え
        try {
            var sqsClientField = SQSEntityBase.class.getDeclaredField("sqsClient");
            sqsClientField.setAccessible(true);
            sqsClientField.set(sqsEntity, mockSqsClient);
        } catch (Exception e) {
            fail("Failed to inject mock SQS client: " + e.getMessage());
        }
    }

    @Test
    void testSendMessage() throws Exception {
        // モックの戻り値を設定
        SendMessageResult mockResult = new SendMessageResult();
        when(mockSqsClient.sendMessage(any(SendMessageRequest.class))).thenReturn(mockResult);

        // メソッドを呼び出し、例外が発生しないことを確認
        SendMessageResult result = sqsEntity.sendMessage();
        assertNotNull(result);

        // sendMessage が正しく呼ばれたか検証
        verify(mockSqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void testGetMessageBody() {
        assertEquals("{\"message\": \"test\"}", sqsEntity.getMessageBody());
    }
}
