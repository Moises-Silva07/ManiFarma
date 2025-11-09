// package dev.java.ManiFarma.Service;
// import dev.java.ManiFarma.DTO.EmployeeRequest;
// import dev.java.ManiFarma.DTO.EmployeeResponse;
// import dev.java.ManiFarma.Entity.Employee;
// import dev.java.ManiFarma.Repository.EmployeeRepository;

// import java.util.List;
// import java.util.stream.Collectors;

// import org.springframework.stereotype.Service;

// @Service
// public class EmployeeService {

//     private final EmployeeRepository repository;

//     public EmployeeService(EmployeeRepository repository) {
//         this.repository = repository;
//     }

//     public EmployeeResponse create(EmployeeRequest request) {
//         // Esta lógica de criação pode ser mantida se for para criar um Employee
//         // que já existe como User, ou se for uma criação interna sem autenticação.
//         // Caso contrário, a criação inicial de um Employee para login deve ser via AuthService.
//         Employee employee = Employee.builder()
//                 .name(request.getName())
//                 .role(request.getRole())
//                 .salary(request.getSalary())
//                 .shift(request.getShift())
//                 .build();

//         Employee saved = repository.save(employee);

//         return toResponse(saved);
//     }

//     public List<EmployeeResponse> findAll() {
//         return repository.findAll()
//                 .stream()
//                 .map(this::toResponse)
//                 .collect(Collectors.toList());
//     }

//     private EmployeeResponse toResponse(Employee employee) {
//         return EmployeeResponse.builder()
//                 .id(employee.getId())
//                 .name(employee.getName())
//                 .role(employee.getRole())
//                 .salary(employee.getSalary())
//                 .shift(employee.getShift())
//                 .build();
//     }
// }