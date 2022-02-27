package ch.epfl.javelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitsTest {
    @Test
    void extractsSignedWorksOnNonTrivialValue (){
        assertEquals(-6, Bits.extractSigned(-889275714, 24, 4));
    }

    @Test
    void extractsSignedThrowExceptionOnTooLargeLength(){
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractSigned(0, 0, 33);
        });
    }

    @Test
    void extractsSignedThrowsExceptionOnInvalidRange(){
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractSigned(0, -3, 4);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractSigned(0, 30, 3);
        });
    }

    @Test
    void extractsUnsignedWorksOnNonTrivialValue (){
        assertEquals(10, Bits.extractUnsigned(-889275714, 24, 4));
    }

    @Test
    void extractsUnsignedThrowsExceptionOnTooLargeLength(){
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractUnsigned(0, 0, 33);
        });
    }

    @Test
    void extractsUnsignedThrowsExceptionOnInvalidRange(){
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractUnsigned(0, -3, 4);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractUnsigned(0, 30, 3);
        });
    }

    @Test
    void extractsUnsignedThrowsExceptionOnLength32(){
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractUnsigned(0, 0, 32);
        });
    }
}