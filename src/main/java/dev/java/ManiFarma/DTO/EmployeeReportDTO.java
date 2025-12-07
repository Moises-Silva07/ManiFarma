package dev.java.ManiFarma.DTO;

public class EmployeeReportDTO {

    private Long employeeId;
    private String employeeName;
    private Long ordersHandled;
    private Double totalValueHandled;

    public EmployeeReportDTO() {
        this.totalValueHandled = 0.0;
    }

    public EmployeeReportDTO(Long employeeId,
                             String employeeName,
                             Long ordersHandled,
                             Double totalValueHandled) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.ordersHandled = ordersHandled;
        this.totalValueHandled = (totalValueHandled != null) ? totalValueHandled : 0.0;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Long getOrdersHandled() {
        return ordersHandled;
    }

    public void setOrdersHandled(Long ordersHandled) {
        this.ordersHandled = ordersHandled;
    }

    public Double getTotalValueHandled() {
        return totalValueHandled;
    }

    public void setTotalValueHandled(Double totalValueHandled) {
        this.totalValueHandled = totalValueHandled;
    }
}
