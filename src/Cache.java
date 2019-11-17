import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A generic Cache managing entities for optimization
 *
 * @param <T> An object that implements Entity
 */
public class Cache<T extends Entity> {
    private final Mode mode;
    private RepositoryProvider repo;
    private java.util.concurrent.locks.Lock dataMutex;
    private java.util.Map<Integer, T> map;
    private java.util.ArrayList<Subscriber<T>> subscribers;
    private Converter<T> converter;

    /**
     * Enum that indicate the operation occurred in cache
     */
    enum Operation {
        ADD, UPDATE, REMOVE
    }

    /**
     * Enum that indicate the mode of the cache
     */
    public enum Mode {
        EAGER, LAZY
    }

    /**
     * Cache constructor
     *
     * @param repositoryProvider A repo to attach
     * @param mode               A mode to operate like
     */
    Cache(RepositoryProvider repositoryProvider, Mode mode, Converter<T> converter) throws RepoAccessException {
        this.mode = mode;
        this.repo = repositoryProvider;
        this.converter = converter;
        this.map = new HashMap<>();
        this.dataMutex = new ReentrantLock();
        if (mode == Mode.EAGER) {
            loadAll();
        }
        this.subscribers = new ArrayList<>();
    }

    /**
     * loads all entries from repo
     *
     * @throws RepoAccessException if loading all entities failed
     */
    private void loadAll() throws RepoAccessException {
        // this implementation is critical to be like this (loading *all* or nothing) because of the logic implies
        // from the mode of the cache to add/update/remove methods
        ArrayList<JSONObject> jsonObjects = repo.getAll();
        if (jsonObjects == null) throw new RepoAccessException();
        for (JSONObject jsonObject : jsonObjects) {
            T entity = converter.getEntity(jsonObject);
            map.put(entity.getId(), entity);
        }
    }

    /**
     * get a cached entity
     *
     * @param eId the id of the entity
     * @return entity required. null if not exists
     */
    T get(Integer eId) {
        // I decided to not inform the user in case of failure in accessing the repo
        // (even though it might be there and not in the cache), because the user doesn't care:
        // * if he can't get it it's not there!! **.
        T entity = map.get(eId);
        if (mode == Mode.LAZY && entity == null) {
            JSONObject jsonObject = repo.get(eId);
            if (jsonObject == null) return null; // not exists in repo
            entity = converter.getEntity(jsonObject);
            if (entity == null) return null; // not exists in Cache (though it might be in repo but not accessible)
            map.put(eId, entity);
        }
        return entity;
    }

    /**
     * add an entity to cache
     *
     * @param entity en entity to add
     * @return true if added successfully, false if already exists
     * @throws RepoAccessException When the repo is not accessible
     */
    boolean add(T entity) throws RepoAccessException {
        // I chose this implementation over a simple one of using Cache.get to save one redundant expansive call to
        // repo.get in Cache.get
        RepositoryProvider.ReturnSate returnSate;
        if (map.get(entity.getId()) != null) return false; // already in map of cache (and also in repo obviously)
        dataMutex.lock();
        returnSate = repo.add(converter.getJson(entity));
        switch (returnSate) {
            case SUCCESS: { // the entity wasn't in repo and should be add to Cache as well
                map.put(entity.getId(), entity);
                dataMutex.unlock();
                notifyUsers(Operation.ADD, entity);
                break;
            }
            case FAILURE: {
                dataMutex.unlock();
                throw new RepoAccessException(entity.getId());
            }
            case ALREADY_EXISTS: {
                if (mode == Mode.LAZY)
                    map.put(entity.getId(), entity); // the entity is in repo but should be added to cache as well
                dataMutex.unlock();
                return false;
            }
        }
        return true;
    }

    /**
     * updates an entity in cache
     *
     * @param entity An entity to update
     * @return true if updated successfully, false if not exists
     * @throws RepoAccessException When the repo is not accessible
     */
    boolean update(T entity) throws RepoAccessException {
        RepositoryProvider.ReturnSate returnSate;
        if (map.get(entity.getId()) == null && mode == Mode.EAGER)
            return false; // not in map of cache (and also in repo obviously)
        dataMutex.lock();
        returnSate = repo.update(converter.getJson(entity));
        switch (returnSate) {
            case SUCCESS: {
                if (mode == Mode.LAZY && map.get(entity.getId()) == null) map.put(entity.getId(), entity);
                else map.replace(entity.getId(), entity);
                dataMutex.unlock();
                notifyUsers(Operation.UPDATE, entity);
                break;
            }
            case FAILURE: {
                dataMutex.unlock();
                throw new RepoAccessException(entity.getId());
            }
            case NOT_EXISTS: {
                dataMutex.unlock();
                return false;
            }
        }
        return true;
    }

    /**
     * removes an entity from cache
     *
     * @param entity An entity to remove
     * @return true if removed successfully false if not exists
     * @throws RepoAccessException When the repo is not accessible
     */
    boolean remove(T entity) throws RepoAccessException {
        RepositoryProvider.ReturnSate returnSate;
        if (map.get(entity.getId()) == null && mode == Mode.EAGER)
            return false; // not in map of cache (and also in repo obviously)
        dataMutex.lock();
        returnSate = repo.remove(converter.getJson(entity));
        switch (returnSate) {
            case SUCCESS: {
                map.remove(entity.getId()); // will remove either way (if exists in map or not)
                dataMutex.unlock();
                notifyUsers(Operation.REMOVE, entity);
                break;
            }
            case FAILURE: {
                dataMutex.unlock();
                throw new RepoAccessException(entity.getId());
            }
            case NOT_EXISTS: {
                dataMutex.unlock();
                return false;
            }
        }
        return true;
    }

    /**
     * register a subscriber to observes updates
     *
     * @param subscriber A subscriber to add
     */
    void register(Subscriber<T> subscriber) {
        this.subscribers.add(subscriber);
    }

    /**
     * Unregister a subscriber from observing updates
     *
     * @param subscriber A subscriber to remove
     */
    void unregister(Subscriber<T> subscriber) {
        this.subscribers.remove(subscriber);
    }

    /**
     * Notify all subscribers for updates in cache
     *
     * @param operation the operation that has been occurred
     * @param entity    the entity modified
     */
    private void notifyUsers(Operation operation, T entity) {
        // I was wandering if it should be a public method from designing point of view.
        // I decided to leave it private because of nobody should call it but the cache itself
        for (Subscriber<T> subscriber : subscribers) {
            subscriber.inform(operation, entity);
        }
    }
}
