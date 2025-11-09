package com.gearup.appointmentservice.controller;

import com.gearup.appointmentservice.entity.Employee;
import com.gearup.appointmentservice.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/available")
    public List<Employee> getAvailableEmployees() {
        log.info("GET /api/employees/available - Fetching active employees");
        return employeeService.getAllActiveEmployees();
    }
}