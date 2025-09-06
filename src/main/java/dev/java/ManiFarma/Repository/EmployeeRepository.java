package dev.java.ManiFarma.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

import dev.java.ManiFarma.Entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
