package copel.sesproductpackage.line;

import java.util.Map;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.sqs.model.SendMessageResult;

import copel.sesproductpackage.line.entity.LineMessagingApiWebhookEntity;
import copel.sesproductpackage.line.entity.SesInfoRegisterRequestSqsEntity;
import copel.sesproductpackage.line.unit.RequestType;

/**
 * 【SES AIアシスタント】
 * Lambdaがリクエストを受け付け、処理を開始するMainクラス.
 * LINE Messaging APIからWebhookで送信されてくるリクエストを受け付ける.
 *
 * @author 鈴木一矢
 *
 */
public class LambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    // =====================================
    // 環境変数
    // =====================================
    /**
     * LINE Messaging APIのChannel Access Token.
     */
    private static final String LINE_CHANNEL_ACCESS_TOKEN = System.getenv("LINE_CHANNEL_ACCESS_TOKEN");
    /**
     * AWS SQS キューURL
     */
    private static final String SQS_QUEUE_URL_SES_AI_REGISTER = System.getenv("SQS_QUEUE_URL_SES_AI_REGISTER");

    // =====================================
    // メソッド
    // =====================================
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        // (0) 開始ログを出力
        context.getLogger().log(input.getHttpMethod() + " " + input.getPath());

        // (1) リクエストボディを出力
        context.getLogger().log("リクエストBody: " + input.getBody());

        // (2) 処理を実施
        // (2-1) レスポンスオブジェクトを作成
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        // (2-2) リクエストボディをLineMessagingApiWebhookEntityに変換する
        LineMessagingApiWebhookEntity requestEntity = new LineMessagingApiWebhookEntity(input.getBody());

        // (2-3) 異常なリクエストの場合、処理を終了する
        if (!requestEntity.isValid()) {
            response.setStatusCode(400);
            response.setBody("{\"message\": \"LINE Messaging APIからWebhookで送信されたリクエスト内容が不正です。\"}");
            response.setHeaders(Map.of("Content-Type", "application/json"));
            return response;
        } else if (!requestEntity.isMessage()) {
            response.setStatusCode(200);
            response.setBody("{\"message\": \"メッセージ送信またはファイル送信以外のリクエストを受け付けたため、処理を終了します。\"}");
            response.setHeaders(Map.of("Content-Type", "application/json"));
            return response;
        }

        try {
            // (2-4) SQSへメッセージを詰める
            SesInfoRegisterRequestSqsEntity sqsEntity
                = new SesInfoRegisterRequestSqsEntity(Regions.AP_NORTHEAST_1, SQS_QUEUE_URL_SES_AI_REGISTER);
            sqsEntity.setRequestType(requestEntity.isFile() ? RequestType.LineFile.getCode() : RequestType.LineMessage.getCode());
            sqsEntity.setFromGroup(requestEntity.isGroup() ? requestEntity.getGroupId() : null);
            sqsEntity.setFromId(requestEntity.getUserId());
            sqsEntity.setFromName(requestEntity.getUserName(LINE_CHANNEL_ACCESS_TOKEN));
            sqsEntity.setRawContent(requestEntity.isFile() ? null : requestEntity.getText());
            sqsEntity.setFileId(requestEntity.isFile() ? requestEntity.getFileId() : null);
            sqsEntity.setFileName(requestEntity.isFile() ? requestEntity.getFileName() : null);
            SendMessageResult result = sqsEntity.sendMessage();

            // (2-5) メッセージ送信成功ログを出力する
            context.getLogger().log("SQSメッセージ送信成功");
            context.getLogger().log("SQS Message ID: " + result.getMessageId());
        } catch (Exception e) {
            // (2-5) 何らかの原因でSQSへのメッセージ送信が失敗した場合、処理を終了する
            context.getLogger().log("SQSメッセージ送信エラー: " + e.getMessage());
            response.setStatusCode(500);
            response.setBody("{\"message\": \"SQSへのメッセージ送信に失敗しました。\"}");
            response.setHeaders(Map.of("Content-Type", "application/json"));
            return response;
        }

         // (3) レスポンスを作成し返却する
        response.setStatusCode(200);
        response.setBody("{\"message\": \"正常に終了しました。\"}");
        response.setHeaders(Map.of("Content-Type", "application/json"));
        return response;
    }
}
