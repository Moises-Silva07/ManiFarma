package dev.java.ManiFarma.DTO;

public class EmployeeReportDTO {
    private Long employeeId;
    private String employeeName;
    private long ordersHandled;
    private Double totalValueHandled; // Double porque Pedido.valorTotal é Double

    public EmployeeReportDTO() {}

    // Construtor usado pela JPQL "new ... "
    public EmployeeReportDTO(Long employeeId, String employeeName, long ordersHandled, Double totalValueHandled) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.ordersHandled = ordersHandled;
        this.totalValueHandled = totalValueHandled;
    }

    // getters / setters
    public Long getEmployeeId(){return employeeId;}
    public void setEmployeeId(Long id){this.employeeId = id;}
    public String getEmployeeName(){return employeeName;}
    public void setEmployeeName(String n){this.employeeName=n;}
    public long getOrdersHandled(){return ordersHandled;}
    public void setOrdersHandled(long c){this.ordersHandled=c;}
    public Double getTotalValueHandled(){return totalValueHandled;}
    public void setTotalValueHandled(Double v){this.totalValueHandled=v;}
}
