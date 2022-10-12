package com.donn.yygh.hosp.repository;

import com.donn.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/26 21:44
 **/
public interface DepartmentRepository extends MongoRepository<Department,String> {
    Department findByHoscodeAndDepcode(String hoscode, String depcode);
}
