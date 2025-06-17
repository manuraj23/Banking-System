package com.bank.bank.Repository;

import com.bank.bank.Entity.Statement;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatementRepository extends MongoRepository<Statement, ObjectId> {
}
