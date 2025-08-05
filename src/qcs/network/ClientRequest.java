package qcs.network;

import java.io.Serializable;

public class ClientRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private RequestType type; // ✅ Use the imported RequestType
    private String username;
    private String password;
    private String message;

    public ClientRequest(RequestType type) {
        this.type = type;
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getMessage() {
        return message;
    }
}
