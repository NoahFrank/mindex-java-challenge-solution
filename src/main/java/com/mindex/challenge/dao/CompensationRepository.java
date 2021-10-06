package com.mindex.challenge.dao;

import com.mindex.challenge.data.Compensation;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface CompensationRepository extends MongoRepository<Compensation, String> {
    // Utilizes an auto-created read query to find a row based on a referenced object's field.
    // To do this use the format: "findBy<ClassName><ClassFieldName>" while capitalizing the first letter of each word
    Compensation findByEmployeeEmployeeId(String employeeId);
}
