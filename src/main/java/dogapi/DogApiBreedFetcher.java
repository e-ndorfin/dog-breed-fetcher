package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        String url = String.format("https://dog.ceo/api/breed/%s/list", breed.toLowerCase());
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                throw new BreedNotFoundException(breed);
            }
            String body = response.body().string();
            JSONObject obj = new JSONObject(body);
            String status = obj.optString("status", "");
            if ("error".equalsIgnoreCase(status) || response.code() == 404) {
                throw new BreedNotFoundException(breed);
            }
            // expected: {"message":["sub1","sub2"], "status":"success"}
            Object message = obj.get("message");
            List<String> result = new ArrayList<>();
            if (message instanceof JSONArray) {
                JSONArray arr = (JSONArray) message;
                for (int i = 0; i < arr.length(); i++) {
                    result.add(arr.getString(i));
                }
            } else if (message instanceof String) {
                // sometimes message could be empty string or similar
                String s = (String) message;
                if (!s.isEmpty()) {
                    result.add(s);
                }
            }
            return result;
        } catch (IOException e) {
            throw new BreedNotFoundException(breed);
        }
    }
}