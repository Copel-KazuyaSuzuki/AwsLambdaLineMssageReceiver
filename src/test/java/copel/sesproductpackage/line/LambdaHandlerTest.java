package copel.sesproductpackage.line;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.sqs.model.SendMessageResult;

import copel.sesproductpackage.line.entity.LineMessagingApiWebhookEntity;
import copel.sesproductpackage.line.entity.SesInfoRegisterRequestSqsEntity;

public class LambdaHandlerTest {

    @Mock private Context context;
    @Mock private APIGatewayProxyRequestEvent requestEvent;
    @Mock private LineMessagingApiWebhookEntity webhookEntity;
    @Mock private SesInfoRegisterRequestSqsEntity sqsEntity;
    @Mock private SendMessageResult sendMessageResult;

    private LambdaHandler lambdaHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        lambdaHandler = new LambdaHandler();

        // Mocking context
        when(context.getLogger()).thenReturn(mock(LambdaLogger.class));

        // Mock environment variables
        System.setProperty("LINE_CHANNEL_ACCESS_TOKEN", "dummy_token");
        System.setProperty("SQS_QUEUE_URL_SES_AI_REGISTER", "dummy_queue_url");

        // Mocking request event
        when(requestEvent.getHttpMethod()).thenReturn("POST");
        when(requestEvent.getPath()).thenReturn("/webhook");
    }

    @Test
    public void testHandleRequest_ValidMessage() throws Exception {
        // Arrange
        String requestBody = "{\"events\": [{\"type\": \"message\", \"message\": {\"type\": \"text\", \"text\": \"Hello\"}}]}";
        when(requestEvent.getBody()).thenReturn(requestBody);

        when(webhookEntity.isValid()).thenReturn(true);
        when(webhookEntity.isMessage()).thenReturn(true);
        when(webhookEntity.isFile()).thenReturn(false);
        when(webhookEntity.isGroup()).thenReturn(false);
        when(webhookEntity.getUserId()).thenReturn("user123");
        when(webhookEntity.getUserName(anyString())).thenReturn("Test User");
        when(sqsEntity.sendMessage()).thenReturn(sendMessageResult);
        when(sendMessageResult.getMessageId()).thenReturn("msg123");

        // Act
        APIGatewayProxyResponseEvent response = lambdaHandler.handleRequest(requestEvent, context);

        // Assert
        assertEquals(200, response.getStatusCode());
        assertEquals("{\"message\": \"正常に終了しました。\"}", response.getBody());
    }

    @Test
    public void testHandleRequest_InvalidRequest() {
        // Arrange
        String requestBody = "{\"events\": [{\"type\": \"message\", \"message\": {\"type\": \"text\", \"text\": \"Hello\"}}]}";
        when(requestEvent.getBody()).thenReturn(requestBody);
        when(webhookEntity.isValid()).thenReturn(false);

        // Act
        APIGatewayProxyResponseEvent response = lambdaHandler.handleRequest(requestEvent, context);

        // Assert
        assertEquals(400, response.getStatusCode());
        assertEquals("{\"message\": \"LINE Messaging APIからWebhookで送信されたリクエスト内容が不正です。\"}", response.getBody());
    }

    @Test
    public void testHandleRequest_NonMessageEvent() {
        // Arrange
        String requestBody = "{\"events\": [{\"type\": \"join\", \"source\": {\"type\": \"user\", \"userId\": \"user123\"}}]}";
        when(requestEvent.getBody()).thenReturn(requestBody);
        when(webhookEntity.isValid()).thenReturn(true);
        when(webhookEntity.isMessage()).thenReturn(false);

        // Act
        APIGatewayProxyResponseEvent response = lambdaHandler.handleRequest(requestEvent, context);

        // Assert
        assertEquals(200, response.getStatusCode());
        assertEquals("{\"message\": \"メッセージ送信またはファイル送信以外のリクエストを受け付けたため、処理を終了します。\"}", response.getBody());
    }

    @Test
    public void testHandleRequest_SqsSendMessageError() throws Exception {
        // Arrange
        String requestBody = "{\"events\": [{\"type\": \"message\", \"message\": {\"type\": \"text\", \"text\": \"Hello\"}}]}";
        when(requestEvent.getBody()).thenReturn(requestBody);
        when(webhookEntity.isValid()).thenReturn(true);
        when(webhookEntity.isMessage()).thenReturn(true);
        when(sqsEntity.sendMessage()).thenThrow(new RuntimeException("SQS Error"));

        // Act
        APIGatewayProxyResponseEvent response = lambdaHandler.handleRequest(requestEvent, context);

        // Assert
        assertEquals(500, response.getStatusCode());
        assertEquals("{\"message\": \"SQSへのメッセージ送信に失敗しました。\"}", response.getBody());
    }
}
