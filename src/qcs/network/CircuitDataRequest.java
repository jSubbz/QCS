package qcs.network;

public class CircuitDataRequest extends ClientRequest {
    private static final long serialVersionUID = 1L;

    private String circuitJson;

    public CircuitDataRequest(String username, String circuitJson) {
        // 1. Set the request type in the parent constructor.
        super(RequestType.CIRCUIT_DATA);
        // 2. Use the parent's setUsername method to store the username.
        super.setUsername(username);
        this.circuitJson = circuitJson;
    }

    public String getCircuitJson() {
        return circuitJson;
    }
}