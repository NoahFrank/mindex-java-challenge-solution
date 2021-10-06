package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.UUID;
import java.util.List;
import java.util.LinkedList;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Finding employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure getReportingStructure(Employee employee) {
        LOG.debug("Getting Reporting Structure employee [{}]", employee);

        // tree traversal counting of employee.directReports
        ReportingStructure reportStructure = new ReportingStructure();
        reportStructure.setEmployee(employee);

        // Breadth First Search (BFS) of employee.directReports using queue to calculate the numberOfReports
        LinkedList<Employee> queue = new LinkedList<>(employee.getDirectReports());
        HashSet<String> visitedEmployeeSet = new HashSet<>();
        int numberOfReports = 0;

        while (queue.size() > 0) {
            // Dequeue first employee
            Employee reportEmployee = queue.poll();

            // Queue each of the employees who report to reportEmployee
            List<Employee> reportingEmployeeList = reportEmployee.getDirectReports();
            if (reportingEmployeeList == null) {
                LOG.debug("Looking up employee in database with id [{}]", reportEmployee.getEmployeeId());
                Employee reportEmployeeFull = employeeRepository.findByEmployeeId(reportEmployee.getEmployeeId());

                if (reportEmployeeFull == null) {
                    LOG.error("Failed to look up employee in database with id [{}]", reportEmployee.getEmployeeId());
                    throw new RuntimeException("Invalid employeeId: " + reportEmployee.getEmployeeId());
                }
                // List can be null if empty and empty just means we don't add any employees to the queue!
                List<Employee> reportingEmployeeFullList = reportEmployeeFull.getDirectReports();
                if (reportingEmployeeFullList != null)
                    queue.addAll(reportingEmployeeFullList);
            } else {
                // Also support if your employee objects have non-null complete direct report employee lists
                queue.addAll(reportingEmployeeList);
            }
                
            // Count the employees we are traversing if they haven't already been counted
            if (!visitedEmployeeSet.contains(reportEmployee.getEmployeeId()))
                numberOfReports++;

            // Mark visited employees to prevent counting duplicates
            visitedEmployeeSet.add(reportEmployee.getEmployeeId());
        }
        reportStructure.setNumberOfReports(numberOfReports);

        LOG.debug("Built Reporting Structure [{}] for employee [{}]", reportStructure, employee);
        return reportStructure;
    }
}
