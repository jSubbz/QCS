package qcs.network;

import java.io.Serializable;
import java.util.List;

public class ServerResponse implements Serializable {
    private boolean success;
    private String message;
    private List<String> circuits; // for LOAD_CIRCUITS

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getCircuits() {
        return circuits;
    }

    public void setCircuits(List<String> circuits) {
        this.circuits = circuits;
    }
}
