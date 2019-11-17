import org.json.*;

/**
 * Entity-JSON converter for cache
 */
public interface Converter<T extends Entity> {
    /**
     * convert json to entity
     *
     * @param jsonObject a json object to convert
     * @return an entity represents the json object given, null if conversion failed
     */
    T getEntity(JSONObject jsonObject);

    /**
     * convert entity to json
     *
     * @param entity an entity to convert
     * @return a json object represents the entity given, null if conversion failed
     */
    JSONObject getJson(T entity);
}
