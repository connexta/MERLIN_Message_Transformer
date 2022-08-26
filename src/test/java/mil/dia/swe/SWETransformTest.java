package mil.dia.swe;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class SWETransformTest {
    @Test
    void testSample() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("trailcam.xml");
        String json = SWETransform.insertSensorToJSON(is);
        System.out.println(json);
        assertNotNull(json);


    }
}