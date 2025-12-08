package dev.java.ManiFarma.DTO;

public class ClientReportDTO {

    private Long clientId;
    private String clientName;
    private String email;
    private Long ordersCount;
    private Double totalSpent;

    // Construtor vazio
    public ClientReportDTO() {
        this.totalSpent = 0.0; // Garantir que totalSpent não seja nulo
    }

    // Construtor com parâmetros - exatamente como a JPQL instancia
    public ClientReportDTO(Long clientId,
                           String clientName,
                           String email,
                           Long ordersCount,
                           Double totalSpent) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.email = email;
        this.ordersCount = ordersCount;
        this.totalSpent = (totalSpent != null) ? totalSpent : 0.0; // Evitar nulo em totalSpent
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(Long ordersCount) {
        this.ordersCount = ordersCount;
    }

    public Double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(Double totalSpent) {
        this.totalSpent = totalSpent;
    }
}
