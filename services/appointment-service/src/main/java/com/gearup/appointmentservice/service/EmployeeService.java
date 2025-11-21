package com.gearup.appointmentservice.service;

import com.gearup.appointmentservice.entity.Employee;
import com.gearup.appointmentservice.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    public List<Employee> getAllActiveEmployees() {
        log.debug("Fetching all active employees");
        return employeeRepository.findByIsActiveTrue();
    }
}
