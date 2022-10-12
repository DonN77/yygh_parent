package com.donn.yygh.hosp.repository;

import com.donn.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/26 11:18
 **/
public interface HospitalRepository extends MongoRepository<Hospital,String> {
    Hospital findByHoscode(String hoscode);

    List<Hospital> findByHosnameLike(String name);

}
