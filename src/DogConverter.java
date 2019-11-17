import org.json.JSONException;
import org.json.JSONObject;

/**
 * Converter supplied for cache to demonstrate the library functionality
 */
public class DogConverter implements Converter<Dog> {

    @Override
    public Dog getEntity(JSONObject jsonObject) {
        try {
            return new Dog((int) jsonObject.get("id"), (String) jsonObject.get("name"), (int) jsonObject.get("height"),
                    (int) jsonObject.get("weight"), (String) jsonObject.get("race"));
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public JSONObject getJson(Dog dog) {
        return new JSONObject(dog);
    }
}
