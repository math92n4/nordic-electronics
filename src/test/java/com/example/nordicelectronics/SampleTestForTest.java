package com.example.nordicelectronics;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SampleTestForTest {

    @Test
    void testingTheTest() {
        assertEquals(5, 5);
    }
}
