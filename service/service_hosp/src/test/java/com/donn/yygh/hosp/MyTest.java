package com.donn.yygh.hosp;

import com.donn.yygh.hosp.bean.Actor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/25 19:12
 **/
@SpringBootTest
public class MyTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    @Qualifier("actorRepository")
    private MongoRepository mongoRepository;

    @Test
    public void testInsert(){
//        mongoTemplate.insert(new Actor("1","黎明",25,true));
//        List<Actor> list = new ArrayList<>();
//        list.add(new Actor("2","郭富城",30,true));
//        list.add(new Actor("3","王菲",35,false));
//        list.add(new Actor("4","张家辉",30,true));
//        mongoTemplate.insert(list,Actor.class);
        mongoTemplate.save(new Actor("5","周星驰",45,false));
    }

    @Test
    public void testDelete(){
//        Query query = new Query();
//        query.addCriteria(Criteria.where("id").is("1"));
//        mongoTemplate.remove(query,Actor.class);
        }

    @Test
    public void testUpdate(){
    }

    @Test
    public void testSelect(){
        Query query = new Query();
        query.addCriteria(Criteria.where("gender").is(true));
        query.with(Sort.by(Sort.Direction.ASC, "age").and(Sort.by(Sort.Direction.DESC, "_id")))
                .skip(0).limit(2);
        List<Actor> actors = mongoTemplate.find(query, Actor.class);
        for (Actor actor : actors) {
            System.out.println(actor);
        }
    }

    @Test
    public void testPage(){
        Actor actor = new Actor();
        actor.setGender(true);
        Example<Actor> example = Example.of(actor);
        Pageable pageable = PageRequest.of(1,2);
        Page<Actor> all = mongoRepository.findAll(example, pageable);
        System.out.println(all.getTotalElements());
        for (Object actor1 : all.getContent()) {
            System.out.println(actor1);
        }
    }
}
