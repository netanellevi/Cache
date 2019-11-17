import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Represents a repository provider for a cache (to access a low layer)
 */
public interface RepositoryProvider {
    enum ReturnSate {SUCCESS, FAILURE, ALREADY_EXISTS, NOT_EXISTS}

    /**
     * get a repo entry
     *
     * @param id number of entry
     * @return a JSON object represents the entry. null if not exists or repo failed to provide
     */
    JSONObject get(Integer id);

    /**
     * get all entries in repo
     *
     * @return List of all entries in the repo, null if failed to provide
     */
    ArrayList<JSONObject> getAll();

    /**
     * add an entry to repo
     *
     * @param jsonObject JSON object to add
     * @return SUCCESS if added successfully, ALREADY_EXISTS if already exists, FAILURE if repo failed to add
     */
    ReturnSate add(JSONObject jsonObject);

    /**
     * update an entry
     *
     * @param jsonObject JSON object to update
     * @return SUCCESS if updated successfully, NOT_EXISTS if not exists, FAILURE if repo failed to update
     */
    ReturnSate update(JSONObject jsonObject);

    /**
     * remove an entry
     *
     * @param jsonObject JSON object to remove
     * @return SUCCESS if removed successfully, NOT_EXISTS if not exists, FAILURE if repo failed to remove
     */
    ReturnSate remove(JSONObject jsonObject);
}
