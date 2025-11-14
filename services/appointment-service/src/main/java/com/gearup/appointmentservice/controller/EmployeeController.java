package com.gearup.appointmentservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.appointmentservice.entity.Employee;
import com.gearup.appointmentservice.service.EmployeeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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