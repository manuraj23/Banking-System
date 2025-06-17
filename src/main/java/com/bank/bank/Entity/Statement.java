package com.bank.bank.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;



@Document(collection = "Statement")
@Data
@NoArgsConstructor
public class Statement {
    @Id
    private ObjectId id;
    private String kind;
    private Float amt;
    private Float updated_bal;
    private LocalDateTime date;
}
