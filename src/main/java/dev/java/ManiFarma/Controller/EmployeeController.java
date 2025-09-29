// package dev.java.ManiFarma.Controller;


// import org.springframework.web.bind.annotation.*;

// import dev.java.ManiFarma.DTO.EmployeeRequest;
// import dev.java.ManiFarma.DTO.EmployeeResponse;
// import dev.java.ManiFarma.Service.EmployeeService;

// import java.util.List;

// @RestController
// @RequestMapping("/employees")
// public class EmployeeController {

//     private final EmployeeService service;

//     public EmployeeController(EmployeeService service) {
//         this.service = service;
//     }

//     @PostMapping
//     public EmployeeResponse create(@RequestBody EmployeeRequest request) {
//         return service.create(request);
//     }

//     @GetMapping
//     public List<EmployeeResponse> findAll() {
//         return service.findAll();
//     }
// }