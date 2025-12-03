package com.example.nordicelectronics.unit.entity;


import com.example.nordicelectronics.entity.BaseEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


public class BaseEntityTest {

    // Concrete subclass for testing
    static class TestEntity extends BaseEntity {}

    @Test
    void softDelete_shouldSetDeletedAt() {
        TestEntity entity = new TestEntity();

        assertNull(entity.getDeletedAt(), "deletedAt should initially be null");

        entity.softDelete();

        assertNotNull(entity.getDeletedAt(), "deletedAt should be set after softDelete");
        assertTrue(entity.getDeletedAt().isBefore(LocalDateTime.now().plusSeconds(1)),
                "deletedAt should be set to current time");
    }

    @Test
    void isDeleted_shouldReturnTrueWhenDeleted() {
        TestEntity entity = new TestEntity();

        assertFalse(entity.isDeleted(), "isDeleted should be false initially");

        entity.softDelete();

        assertTrue(entity.isDeleted(), "isDeleted should return true after softDelete");
    }

    @Test
    void isDeleted_shouldReturnFalseWhenNotDeleted() {
        TestEntity entity = new TestEntity();

        assertFalse(entity.isDeleted(), "isDeleted should return false if not deleted");
    }
}
