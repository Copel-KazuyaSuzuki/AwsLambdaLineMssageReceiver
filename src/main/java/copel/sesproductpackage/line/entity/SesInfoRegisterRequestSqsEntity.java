package copel.sesproductpackage.line.entity;

import com.amazonaws.regions.Regions;

/**
 * AwsLambdaSesInfoRegisterに付帯するSQSへのリクエストEntityクラス.
 *
 * @author 鈴木一矢
 *
 */
public class SesInfoRegisterRequestSqsEntity extends SQSEntityBase {
    /**
     * リクエスト種別
     */
    private String requestType;
    /**
     * 送信元グループ.
     */
    private String fromGroup;
    /**
     * 送信者ID.
     */
    private String fromId;
    /**
     * 送信者名.
     */
    private String fromName;
    /**
     * 原文.
     */
    private String rawContent;
    /**
     * ファイルID.
     */
    private String fileId;
    /**
     * ファイル名.
     */
    private String fileName;

    /**
     * コンストラクタ.
     *
     * @param region リージョン
     * @param queueUrl SQSのURL
     */
    public SesInfoRegisterRequestSqsEntity(Regions region, String queueUrl) {
        super(region, queueUrl);
    }

    @Override
    protected String getMessageBody() {
        String body = "{"
                + "\"request_type\": \"" + this.requestType + "\","
                + "\"from_group\": \"" + this.fromGroup + "\","
                + "\"from_id\": \"" + this.fromId + "\","
                + "\"from_name\": \"" + this.fromName + "\",";
        if (this.rawContent != null) {
        	body += "\"raw_content\": \"" + this.rawContent + "\"";
        }
        if (this.fileId != null) {
        	body += "\"file_id\": \"" + this.fileId + "\",";
        }
        if (this.fileName != null) {
        	body += "\"file_name\": \"" + this.fileName + "\"";
        }
    	body += "}";
    	return body;
    }

    @Override
    public String toString() {
        return this.getMessageBody();
    }

    // GETTER・SETTER
    public String getRequestType() {
        return requestType;
    }
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    public String getFromGroup() {
        return fromGroup;
    }
    public void setFromGroup(String fromGroup) {
        this.fromGroup = fromGroup;
    }
    public String getFromId() {
        return fromId;
    }
    public void setFromId(String fromId) {
        this.fromId = fromId;
    }
    public String getFromName() {
        return fromName;
    }
    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
    public String getRawContent() {
        return rawContent;
    }
    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }
    public String getFileId() {
        return fileId;
    }
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
