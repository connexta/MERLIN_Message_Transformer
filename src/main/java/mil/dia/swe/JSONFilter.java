package mil.dia.swe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.thisptr.jackson.jq.Scope;

import java.util.List;

public class JSONFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSONFilter.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Share an instance of the root scope object across all instances - they are thread safe
    private static Scope ROOT_SCOPE = Scope.newEmptyScope();
    // JsonQuery objects are immutable and thread-safe, can be reused across objects and threads
    private static JsonQuery DEFAULT_QUERY;
    static {
        // initialize the jq scope
        ROOT_SCOPE.loadFunctions(Scope.class.getClassLoader());
        // set up an identity query in case the configuration is invalid
        try {
            DEFAULT_QUERY = JsonQuery.compile(".");
        } catch (JsonQueryException e) {
            LOGGER.error("Unable to compile default query");
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
}
