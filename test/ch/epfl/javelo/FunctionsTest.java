package ch.epfl.javelo;

import org.junit.jupiter.api.Test;

import java.util.function.DoubleUnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class FunctionsTest {
    @Test
    void constantWorksOnNonTrivialValue(){
        DoubleUnaryOperator constant = Functions.constant(3);
        assertEquals(3, constant.applyAsDouble(18));
    }

    @Test
    void sampledWorksOnNonTrivialValues(){
        float[] samples = {6,4,8,2,10,8};
        double xMax = 10;
        DoubleUnaryOperator sampled = Functions.sampled(samples, xMax);
        assertEquals(5, sampled.applyAsDouble(5));
        assertEquals(8, sampled.applyAsDouble(10));
        assertEquals(6, sampled.applyAsDouble(0));
        assertEquals(8, sampled.applyAsDouble(7.5));
        assertEquals(8.5, sampled.applyAsDouble(9.5));
        assertEquals(5.8, sampled.applyAsDouble(0.2));
        assertEquals(6, sampled.applyAsDouble(0));
        assertEquals(8, sampled.applyAsDouble(234));
        assertEquals(6, sampled.applyAsDouble(-34));
    }


}