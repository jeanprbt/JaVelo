package ch.epfl.javelo.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeSetTest {

    @Test
    void constructorThrowsWhenInvalidBits(){
        assertThrows(IllegalArgumentException.class, () ->  {
            new AttributeSet(0b0110_0100_1110_0000_0000_0000_0000_1110_1111_0011_0000_0000_0000_0000_0000_0000L);
        });
    }

    @Test
    void ofCreatesGoodAttributeSetOnNonTrivialAttributes(){
        AttributeSet attr = AttributeSet.of(Attribute.HIGHWAY_SERVICE, Attribute.HIGHWAY_TRACK, Attribute.HIGHWAY_RESIDENTIAL);
        assertEquals(new AttributeSet(0b0111L), attr);
    }

    @Test
    void containsWorksOnNonTrivialValue() {
        AttributeSet testBits = new AttributeSet(0b0110_1100_0100L);
        //long testMask = 1L << Attribute.HIGHWAY_RESIDENTIAL.ordinal();
        assertEquals(true, testBits.contains(Attribute.HIGHWAY_RESIDENTIAL));
    }
}