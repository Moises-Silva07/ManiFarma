package dev.java.ManiFarma.DTO;

public class ReportSummaryDTO {

    private long totalPedidos;
    private double receitaTotal;
    private long pendentes;
    private long pagos;
    private long concluidos;
    private long cancelados;

    public ReportSummaryDTO() {
    }

    public ReportSummaryDTO(long totalPedidos, double receitaTotal,
                            long pendentes, long pagos,
                            long concluidos, long cancelados) {
        this.totalPedidos = totalPedidos;
        this.receitaTotal = receitaTotal;
        this.pendentes = pendentes;
        this.pagos = pagos;
        this.concluidos = concluidos;
        this.cancelados = cancelados;
    }

    // Getters e Setters

    public long getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(long totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public double getReceitaTotal() {
        return receitaTotal;
    }

    public void setReceitaTotal(double receitaTotal) {
        this.receitaTotal = receitaTotal;
    }

    public long getPendentes() {
        return pendentes;
    }

    public void setPendentes(long pendentes) {
        this.pendentes = pendentes;
    }

    public long getPagos() {
        return pagos;
    }

    public void setPagos(long pagos) {
        this.pagos = pagos;
    }

    public long getConcluidos() {
        return concluidos;
    }

    public void setConcluidos(long concluidos) {
        this.concluidos = concluidos;
    }

    public long getCancelados() {
        return cancelados;
    }

    public void setCancelados(long cancelados) {
        this.cancelados = cancelados;
    }
}
