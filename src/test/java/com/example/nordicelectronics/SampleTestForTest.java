package com.example.nordicelectronics;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class SampleTestForTest {

    @Test
    void testingTheTest() {
        assertEquals(5, 5);
    }
}
