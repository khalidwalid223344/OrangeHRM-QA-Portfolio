package Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class JsonReader {

    public static JsonNode getTestData(String testName) {

        try {

            ObjectMapper mapper = new ObjectMapper();

            InputStream input = JsonReader.class
                    .getClassLoader()
                    .getResourceAsStream("TestData.json");

            if (input == null) {
                throw new RuntimeException("TestData.json not found.");
            }

            JsonNode root = mapper.readTree(input);

            JsonNode testData = root.get(testName);

            if (testData == null) {
                throw new RuntimeException(
                        "No test data found for: " + testName);
            }

            return testData;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
