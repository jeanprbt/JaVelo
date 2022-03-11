package ch.epfl.javelo.data;

import ch.epfl.javelo.Math2;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Test
    void loadFromWorksProperlyWithGivenArchive() throws IOException {
        IntBuffer nodesBuffer ;
        try (InputStream stream = new FileInputStream("lausanne/nodes.bin")) {
           byte[] nodesBytes = stream.readAllBytes();
           ByteBuffer nodesByteBuffer = ByteBuffer.wrap(nodesBytes);
           int[] nodes = new int[Math2.ceilDiv(nodesByteBuffer.capacity(), 4)];
           int arrayIndex = 0 ;
           for (int i = 0; i <  nodesByteBuffer.capacity(); i+=4) {
                nodes[arrayIndex++] = nodesByteBuffer.getInt(i);
           }
           nodesBuffer = IntBuffer.wrap(nodes);
        }

        ByteBuffer sectorsBuffer ;
        try (InputStream stream = new FileInputStream("lausanne/sectors.bin")) {
            byte[] sectorBytes = stream.readAllBytes();
            sectorsBuffer = ByteBuffer.wrap(sectorBytes);
        }

        ByteBuffer edgesBuffer;
        try (InputStream stream = new FileInputStream("lausanne/profile_ids.bin")) {
            byte[] edgesBytes = stream.readAllBytes();
            edgesBuffer = ByteBuffer.wrap(edgesBytes);
        }

        IntBuffer profileIdsBuffer ;
        try (InputStream stream = new FileInputStream("lausanne/nodes.bin")) {
            byte[] profileIdsBytes = stream.readAllBytes();
            ByteBuffer profileIdsByteBuffer = ByteBuffer.wrap(profileIdsBytes);
            int[] profileIds = new int[Math2.ceilDiv(profileIdsByteBuffer.capacity(), 4)];
            int arrayIndex = 0 ;
            for (int i = 0; i < profileIdsByteBuffer.capacity(); i+=4) {
                profileIds[arrayIndex++] = profileIdsByteBuffer.getInt(i);
            }
            profileIdsBuffer = IntBuffer.wrap(profileIds);
        }

        ShortBuffer elevationsBuffer;
        try(InputStream stream = new FileInputStream("lausanne/elevations.bin")) {
            byte[] elevationsBytes = stream.readAllBytes();
            ByteBuffer elevationsByteBuffer = ByteBuffer.wrap(elevationsBytes);
            short[] elevations = new short[Math2.ceilDiv(elevationsByteBuffer.capacity(), 2)];
            int arrayIndex = 0 ;
            for (int i = 0; i < elevationsByteBuffer.capacity(); i+=2) {
                elevations[arrayIndex++] = elevationsByteBuffer.getShort(i);
            }
            elevationsBuffer = ShortBuffer.wrap(elevations);
        }

        List<AttributeSet> attributeSetList = new ArrayList<>();
        LongBuffer attributeSetBuffer;
        try(InputStream stream = new FileInputStream("lausanne/attributes.bin")) {
            byte[] attributesBytes = stream.readAllBytes();
            ByteBuffer attributesByteBuffer = ByteBuffer.wrap(attributesBytes);
            int arrayIndex = 0 ;
            long[] attributesSet = new long[Math2.ceilDiv(attributesByteBuffer.capacity(), 8)];
            for (int i = 0; i < attributesByteBuffer.capacity(); i+=8) {
                attributesSet[arrayIndex++] = attributesByteBuffer.getLong(i);
            }
            attributeSetBuffer = LongBuffer.wrap(attributesSet);
        }
        for (int i = 0; i < attributeSetBuffer.capacity(); i++) {
            attributeSetList.add(new AttributeSet(attributeSetBuffer.get(i)));
        }

        Graph expectedGraph = new Graph(new GraphNodes(nodesBuffer), new GraphSectors(sectorsBuffer), new GraphEdges(edgesBuffer, profileIdsBuffer,elevationsBuffer), attributeSetList);
        Path basePath = Path.of("lausanne");
        Graph graph = Graph.loadFrom(basePath) ;
        assertEquals(expectedGraph, graph);
    }
}