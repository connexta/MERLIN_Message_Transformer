package mil.dia.swe;

import net.thisptr.jackson.jq.exception.JsonQueryException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class JSONFilterTest {

  static String JSONstr;
  static String PD = ".procedureDescription";
  static String PREF = PD + " | ";
  static String PROCS = "(.identification.identifier| map({(.name): (.value)}) | add)";
  static String CHARS = "(if .classification.classifier? then .classification.classifier | map(if .name? then {(.name): (.value)} else null end) | add else null end)";
  static String TIME = "(.validTime | if .type == \"TimeInstant\" then {\"validTimeStart\": (.timePosition.value)} else {\"validTimeStart\": (.beginPosition), \"validTimeEnd\": (.endPosition)} end)";
  static String SEC = "(if .securityConstraint? then {\"securityConstraint\": .securityConstraint} else null end)";
  static String LEG = "(if .legalConstraint? then {\"legalConstraint\": .legalConstraint} else null end)";
  static String LOC = "(if .member.location? then {\"sensorLocation\": (.member.location)} else null end)";
  static String QUERY = PREF +
                        PROCS  + " + " +
                        CHARS + " + " +
                        TIME + " + " +
                        SEC + " + " +
                        LEG + " + " +
                        LOC;
  @BeforeAll
  static void initTestJson() throws IOException {
    JSONstr = new String(Files.readAllBytes(Paths.get("./src/test/resources/trailcam.json")));
  }

  @Test
  void filter() throws JsonQueryException {
    // JSONFilter filter = new JSONFilter(".procedureDescription | (.identification.identifier|
    // map({(.name): (.value)}) | add) + (.classification.classifier | map(if .name? then {(.name):
    // (.value)} else null end) | add)");
    JSONFilter filter =
        new JSONFilter(
            ".procedureDescription | (.identification.identifier| map({(.name): (.value)}) | add) + (.classification.classifier | map(if .name? then {(.name): (.value)} else null end) | add) + (.validTime | if .type == \"TimeInstant\" then {\"validTimeStart\": (.timePosition.value)} else {\"validTimeStart\": (.beginPosition), \"validTimeEnd\": (.endPosition)} end) + (if .securityConstraint? then {\"securityConstraint\": .securityConstraint} else null end) + (if .legalConstraint? then {\"legalConstraint\": .legalConstraint} else null end) + (if .member.location? then {\"sensorLocation\": (.member.location)} else null end)");
    String result = filter.filter(JSONstr);
    System.out.println(result);
    assertNotNull(result);
  }

  @Test
  void testProcedureDescription() throws JsonQueryException {
    JSONFilter filter = new JSONFilter(PD);
    String result = filter.filter(JSONstr);
    System.out.println(result);
    assertNotNull(result);
  }

  @Test
  void testProcessIds() throws JsonQueryException {
    JSONFilter filter = new JSONFilter(PREF + PROCS);
    String result = filter.filter(JSONstr);
    System.out.println(result);
    assertEquals("{\"uniqueID\":\"urn:ogc:object:Sensor:Trailcam\",\"longName\":\"Super Secret Trailcam\",\"shortName\":\"Trailcam\"}", result);
  }

  @Test
  void testProcessCharacteristics() throws JsonQueryException {
    JSONFilter filter = new JSONFilter(PREF + CHARS);
    String result = filter.filter(JSONstr);
    System.out.println(result);
    assertEquals("null", result);
  }

  @Test
  void testValidTime() throws JsonQueryException {
    JSONFilter filter = new JSONFilter(PREF + TIME);
    String result = filter.filter(JSONstr);
    System.out.println(result);
    assertEquals("{\"validTimeStart\":\"2020-01-01T00:00:01Z\"}", result);
  }

  @Test
  void testSecurityConstraint() throws JsonQueryException {
    JSONFilter filter = new JSONFilter(PREF + SEC);
    String result = filter.filter(JSONstr);
    System.out.println(result);
    assertEquals("null", result);
  }

  @Test
  void testLegalConstraint() throws JsonQueryException {
    JSONFilter filter = new JSONFilter(PREF + LEG);
    String result = filter.filter(JSONstr);
    System.out.println(result);
    assertEquals("null", result);
  }

  @Test
  void testLocation() throws JsonQueryException {
    JSONFilter filter = new JSONFilter(PREF + LOC);
    String result = filter.filter(JSONstr);
    System.out.println(result);
    assertEquals(
        "{\"sensorLocation\":{\"srsName\":\"EPSG:4979\",\"srsDimension\":\"3\",\"pos\":\"39.69427 -84.10164 0.0\",\"id\":\"sensorLocation\",\"type\":\"Point\"}}",
        result);
  }

  @Test
  void testCompleteQuery() throws JsonQueryException {
    System.out.println("QUERY: " + QUERY);
    JSONFilter filter = new JSONFilter(QUERY);
    String result = filter.filter(JSONstr);
    System.out.println(result);
    assertEquals(
        "{\"uniqueID\":\"urn:ogc:object:Sensor:Trailcam\",\"longName\":\"Super Secret Trailcam\",\"shortName\":\"Trailcam\",\"validTimeStart\":\"2020-01-01T00:00:01Z\",\"sensorLocation\":{\"srsName\":\"EPSG:4979\",\"srsDimension\":\"3\",\"pos\":\"39.69427 -84.10164 0.0\",\"id\":\"sensorLocation\",\"type\":\"Point\"}}",
        result);
  }
}
