package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.document.ReviewDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewMongoRepository extends MongoRepository<ReviewDocument, String> {
    List<ReviewDocument> findByProductId(String productId);
    List<ReviewDocument> findByUserId(String userId);
}

