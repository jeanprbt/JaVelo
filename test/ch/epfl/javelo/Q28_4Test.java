package ch.epfl.javelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Q28_4Test {
    @Test
    void ofIntWorksOnNonTrivialValue(){
        assertEquals(0b0110_1010_0000, Q28_4.ofInt(0b0110_1010));
    }

    @Test
    void asDoubleWorksOnNonTrivialValue(){
        assertEquals(5.625, Q28_4.asDouble(0b0101_1010));
    }

    @Test
    void asDoubleWorksOnTrivialValue(){
        assertEquals(0.0, Q28_4.asDouble(0b0000_0000_0000));
    }

    @Test
    void asFloatWorksOnNonTrivialValue(){
        assertEquals(5.625, Q28_4.asFloat(0b0101_1010));
    }

    @Test
    void asFloatWorksOnTrivialValue(){
        assertEquals(0.0, Q28_4.asFloat(0b0000_0000_0000));
    }

}