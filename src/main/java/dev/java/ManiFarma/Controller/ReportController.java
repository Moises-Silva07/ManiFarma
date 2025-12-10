package dev.java.ManiFarma.Controller;

// Imports...
import dev.java.ManiFarma.DTO.*;
import dev.java.ManiFarma.Entity.Pedido;
import dev.java.ManiFarma.Service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryDTO> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {

        LocalDateTime start = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : null;

        return ResponseEntity.ok(service.getSummary(start, end));
    }


    @GetMapping("/orders")
    public ResponseEntity<List<Pedido>> orders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDateTime start = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : null;
        return ResponseEntity.ok(service.getOrdersBetween(start, end));
    }

    @GetMapping("/clients/top")
    public ResponseEntity<List<ClientReportDTO>> topClients(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit) {
        LocalDateTime start = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : null;
        return ResponseEntity.ok(service.getTopClients(start, end, limit));
    }

    @GetMapping("/employees/top")
    public ResponseEntity<List<EmployeeReportDTO>> topEmployees(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit) {
        LocalDateTime start = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : null;
        return ResponseEntity.ok(service.getTopEmployees(start, end, limit));
    }
}