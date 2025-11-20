package theknifeserver;

import java.io.Serializable;

public class ServerResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String status;  // "OK" o "ERROR"
    private Object payload; // può contenere DTO o liste di DTO

    public ServerResponse(String status, Object payload) {
        this.status = status;
        this.payload = payload;
    }

    public String getStatus() {
        return status;
    }

    public Object getPayload() {
        return payload;
    }
}
