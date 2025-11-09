package dev.java.ManiFarma.DTO;

public class ClientReportDTO {
    private Long clientId;
    private String clientName;
    private String email;
    private long ordersCount;
    private Double totalSpent;

    public ClientReportDTO() {}

    public ClientReportDTO(Long clientId, String clientName, String email, long ordersCount, Double totalSpent) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.email = email;
        this.ordersCount = ordersCount;
        this.totalSpent = totalSpent;
    }

    // getters / setters
    public Long getClientId(){return clientId;}
    public void setClientId(Long id){this.clientId = id;}
    public String getClientName(){return clientName;}
    public void setClientName(String n){this.clientName=n;}
    public String getEmail(){return email;}
    public void setEmail(String e){this.email=e;}
    public long getOrdersCount(){return ordersCount;}
    public void setOrdersCount(long c){this.ordersCount=c;}
    public Double getTotalSpent(){return totalSpent;}
    public void setTotalSpent(Double v){this.totalSpent=v;}
}
