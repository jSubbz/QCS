package qcs.network;

public class CircuitDataRequest extends ClientRequest {
    private static final long serialVersionUID = 1L;

    private String username;
    private String circuitJson;

    public CircuitDataRequest(String username, String circuitJson) {
        super(RequestType.CIRCUIT_DATA);
        this.username = username;
        this.circuitJson = circuitJson;
    }

    public String getUsername() {
        return username;
    }

    public String getCircuitJson() {
        return circuitJson;
    }
}
