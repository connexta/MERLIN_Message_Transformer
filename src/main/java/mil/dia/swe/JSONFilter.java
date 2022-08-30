package mil.dia.swe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.thisptr.jackson.jq.Scope;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class JSONFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(JSONFilter.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  // Share an instance of the root scope object across all instances - they are thread safe
  private static Scope ROOT_SCOPE = Scope.newEmptyScope();
  // JsonQuery objects are immutable and thread-safe, can be reused across objects and threads
  private static JsonQuery DEFAULT_QUERY;

  // TEMPORARY FOR DEMO PURPOSES ONLY
  protected static String MOCK_UI_TRANSFORM_RESPONSE;

  static {
    // initialize the jq scope
    ROOT_SCOPE.loadFunctions(Scope.class.getClassLoader());
    // set up an identity query in case the configuration is invalid
    try {
      DEFAULT_QUERY = JsonQuery.compile(".");
    } catch (JsonQueryException e) {
      LOGGER.error("Unable to compile default query");
    }

    // TEMPORARY FOR DEMO PURPOSES ONLY
    try {
      Class thisClass = JSONFilter.class;
      URL url = JSONFilter.class.getClassLoader().getResource("mock-ui-transform.json");
      File file = new File(url.getFile());
      MOCK_UI_TRANSFORM_RESPONSE = new String(Files.readAllBytes(file.toPath()));
    } catch (IllegalArgumentException | IOException e) {
      LOGGER.error("Unable to read mock response file");
      MOCK_UI_TRANSFORM_RESPONSE =
          "{\"URN\": \"urn:ogc:object:Sensor:example:EX123\",\n"
              + "    \"uniqueID\": \"urn:ogc:object:Sensor:example:EX123\",\n"
              + "    \"longName\": \"EX123-Example\",\n"
              + "    \"shortName\": \"EX123-Example\",\n"
              + "    \"manufacturer\": \"Acme\"}";
    }
  }

  // Each instance has its own query
  JsonQuery q = DEFAULT_QUERY;

  public JSONFilter(String filter) throws JsonQueryException {
    initFilter(filter);
  }

  public void initFilter(String filter) throws JsonQueryException {
    q = JsonQuery.compile(filter);
  }

  public String filter(String jsonStr) {
    Scope childScope = Scope.newChildScope(ROOT_SCOPE);
    List<JsonNode> filteredResult;
    String result = "";
    try {
      JsonNode json = MAPPER.readTree(jsonStr);
      filteredResult = q.apply(childScope, json);
      result = filteredResult.get(0).toString();
    } catch (JsonProcessingException e) {
      LOGGER.error("Error filtering JSON string: " + e.getMessage(), e);
    }
    return result;
  }


  // TEMPORARY FOR DEMO PURPOSES ONLY
  public static String mockFilter(String json) {
    return MOCK_UI_TRANSFORM_RESPONSE;
  }
}
