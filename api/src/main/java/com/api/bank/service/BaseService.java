package com.api.bank.service;

import com.api.bank.model.entity.Account;
import com.api.bank.model.entity.Base;
import com.api.bank.model.ObjectResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

public class BaseService<T extends Base, T1 extends JpaRepository<T, UUID>> {
    private T1 repository;
    private T entity;
    @Autowired
    private ObjectMapper objectMapper;

    public ObjectResponse add(Account data) {
        try {
//            T entity = (T) objectMapper.convertValue(data, Object.class);
            System.out.println("add");
            repository.save(entity);
            repository.flush();

            return new ObjectResponse("Success", entity, true);
        } catch (Exception e) {
            return new ObjectResponse(e.getMessage(),false);
        }
    }

    public ObjectResponse remove(T entity) {
        try {
            System.out.println("remove");

            repository.delete(entity);
            repository.flush();
            return new ObjectResponse("Success", true);
        } catch (Exception e) {
            return new ObjectResponse(e.getMessage(),false);
        }
    }

    public ObjectResponse update(T entity) {
        try {
            System.out.println("update");
            T originEntity = repository.findById(entity.getId()).get(); // get the entity from the database

            repository.save(originEntity);
            repository.flush();
            return new ObjectResponse("Success", entity, true);
        } catch (Exception e) {
            return new ObjectResponse(e.getMessage(),false);
        }
    }
        public ObjectResponse get (UUID id){
            try {
                System.out.println("get");
                T entity = repository.findById(id).get();
                return new ObjectResponse("Success", entity, true);
            } catch (Exception e) {
                return new ObjectResponse("Error", false);
            }
        }



}