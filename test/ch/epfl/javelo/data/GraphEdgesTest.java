package ch.epfl.javelo.data;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.*;

class GraphEdgesTest {
    @Test
    void profileSamplesWorksWithOsAtEnd(){
        ByteBuffer edgesBuffer = ByteBuffer.allocate(1000);
        IntBuffer profileIds = IntBuffer.wrap(new int[]{
                
        }) ;
        for (int i = 0; i < 1000; i++) {
            edgesBuffer.putInt(i);
            edgesBuffer.putShort((short)3);
            edgesBuffer.putShort((short)3);
            edgesBuffer.putShort((short)i);
        }

    }
}