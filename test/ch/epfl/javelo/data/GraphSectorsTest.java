package ch.epfl.javelo.data;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphSectorsTest {

    @Test
    void sectorsInAreaWorksOnNonTrivialValues(){

    }

    @Test
    void sectorsInAreaWorksOnOneSector() {
        ByteBuffer sectorBuffer = ByteBuffer.allocate(40);
        sectorBuffer.putInt(0);
        sectorBuffer.putShort((short)100);
        List<GraphSectors.Sector> sectorList = new ArrayList<>();
        sectorList.add(new GraphSectors.Sector(0, 101));
        GraphSectors graph = new GraphSectors(sectorBuffer);
        assertEquals(sectorList, graph.sectorsInArea(new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N), 100));
    }
}