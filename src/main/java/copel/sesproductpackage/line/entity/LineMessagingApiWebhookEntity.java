package copel.sesproductpackage.line.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * LINE Messaging APIからWebhookで送信されてくるリクエストのEntityクラス.
 *
 * @author 鈴木一矢
 */
public class LineMessagingApiWebhookEntity {
    /**
     * リクエスト種別("message", "file", "join", etc.).
     */
    private String type;
    /**
     * 送信元種別("user", "group").
     */
    private String sourceType;
    /**
     * 送信元ユーザーID.
     */
    private String userId;
    /**
     * 送信元グループID.
     */
    private String groupId;
    /**
     * メッセージ種別("text", "image", "file", etc.).
     */
    private String messageType;
    /**
     * メッセージ本文.
     */
    private String text;
    /**
     * タイムスタンプ.
     */
    private LocalDateTime timestamp;
    /**
     * ファイル名.
     */
    private String fileName;
    /**
     * ファイルID.
     */
    private String fileId;

    /**
     * JSON文字列を受け取ってパースし、メンバ変数に格納.
     *
     * @param jsonString JSON文字列
     * @throws IOException
     */
    public LineMessagingApiWebhookEntity(final String jsonString) throws IllegalArgumentException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);

            // eventsが空であれば処理しない
            if (!rootNode.has("events")) {
                throw new IOException();
            } else if (rootNode.path("events").size() < 1) {
                throw new IOException();
            }

            // イベントのタイプ (message, follow, join など)
            this.type = rootNode.path("events").get(0).path("type").asText();

            // 送信日時
            Long timestampMills = rootNode.path("events").get(0).path("timestamp").asLong() / 1000;
            this.timestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestampMills), ZoneOffset.UTC);

            // 送信元の情報 (user or group)
            JsonNode sourceNode = rootNode.path("events").get(0).path("source");
            this.sourceType = sourceNode.path("type").asText();
            this.userId = sourceNode.path("userId").asText();
            this.groupId = sourceNode.has("groupId") ? sourceNode.get("groupId").asText() : null;

            // メッセージの種類
            if (this.isMessage()) {
                this.messageType = rootNode.path("events").get(0).path("message").path("type").asText();
                this.text = rootNode.path("events").get(0).path("message").path("text").asText();
                // ファイル送信なら、ファイルIDとファイル名を取得する.
                if (this.isFile()) {
                    this.fileId = rootNode.path("events").get(0).path("message").path("id").asText();
                    this.fileName = rootNode.path("events").get(0).path("message").path("fileName").asText();
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid JSON format");
        }
    }

    /**
     * Webhookのリクエストが正しいフォーマットかチェック.
     *
     * @return 正しければtrue
     */
    public boolean isValid() {
        return this.type != null && !this.type.isEmpty() && this.sourceType != null && !this.sourceType.isEmpty();
    }

    /**
     * メッセージイベントか判定.
     *
     * @return メッセージならtrue
     */
    public boolean isMessage() {
        return "message".equals(this.type);
    }

    /**
     * ファイルが送信されたリクエストか判定.
     *
     * @return ファイル送信ならtrue
     */
    public boolean isFile() {
        return isMessage() && "file".equals(this.messageType);
    }

    /**
     * グループ内で受信したメッセージか判定.
     *
     * @return グループならtrue
     */
    public boolean isGroup() {
        return "group".equals(this.sourceType) && this.groupId != null && !this.groupId.isEmpty();
    }

    /**
     * 個人LINEで受信したメッセージか判定.
     *
     * @return 個人ならtrue
     */
    public boolean isPersonal() {
        return "user".equals(this.sourceType);
    }

    /**
     * このメッセージの送信者名を取得する.
     *
     * @return 送信者名
     */
    public String getUserName(final String channnelAccessToken) {
        // このクラスのthis.userIdを使用してユーザー名を取得し返却する
        try {
            // LINE APIを呼び出してユーザー名を取得
            String apiUrl = "https://api.line.me/v2/bot/profile/" + this.userId;
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + channnelAccessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // レスポンスを読み取る
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // JSONをパース
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.toString());
                String userName = rootNode.path("displayName").asText();
                return userName;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * このメッセージが送信されたグループ名を取得する.
     *
     * @return グループ名
     */
    public String getGroupName(final String channnelAccessToken) {
        if (this.isGroup()) {
            try {
                // LINE APIを呼び出してグループ名を取得
                String apiUrl = "https://api.line.me/v2/bot/group/" + this.groupId + "/summary";
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + channnelAccessToken);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // レスポンスを読み取る
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // JSONをパース
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(response.toString());
                    String groupName = rootNode.path("groupName").asText();
                    return groupName;
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    // GETTER
    public String getType() {
        return type;
    }
    public String getSourceType() {
        return sourceType;
    }
    public String getUserId() {
        return userId;
    }
    public String getGroupId() {
        return groupId;
    }
    public String getMessageType() {
        return messageType;
    }
    public String getText() {
        return text;
    }
    public String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return this.timestamp != null ? this.timestamp.format(formatter) : null;
    }
    public String getFileName() {
        return this.fileName;
    }
    public String getFileId() {
        return this.fileId;
    }
}
