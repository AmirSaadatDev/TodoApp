package common.protocol;

import com.google.gson.JsonElement;

public class Response {
    private String status;
    private String message;
    private JsonElement data;

    private Response(String status, String message, JsonElement data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static Response success(String message) {
        return new Response("success", message, null);
    }

    public static Response success(String message, JsonElement data) {
        return new Response("success", message, data);
    }

    public static Response error(String message) {
        return new Response("error", message, null);
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public JsonElement getData() { return data; }
    public boolean isSuccess() { return "success".equals(status); }
}
