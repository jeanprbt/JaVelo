package ch.epfl.javelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Q28_4Test {
    @Test
    void ofIntWorksOnNonTrivialValue(){
        assertEquals(0b0000_0000_0000_0000_0000_0000_1010_0000, Q28_4.ofInt(0b1010_0000_0000_0000_0000_0000_0000_1010));
    }


}