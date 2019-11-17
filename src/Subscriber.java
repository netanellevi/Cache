/**
 * A subscriber for a cache
 */
public interface Subscriber<T extends Entity> {
    /**
     * get an entity cached
     *
     * @param eId id of the entity
     * @return the entity required. null if not exists
     */
    T get(Integer eId);

    /**
     * add an entity to cache
     *
     * @param entity an entity to add
     * @return true if added successfully false if already exits
     * @throws RepoAccessException When the repo is not accessible
     */
    boolean add(T entity) throws RepoAccessException;

    /**
     * update an entity in cache
     *
     * @param entity an entity to update
     * @return true if updated successfully false if not exists
     * @throws RepoAccessException When the repo is not accessible
     */
    boolean update(T entity) throws RepoAccessException;

    /**
     * remove an entity from cache
     *
     * @param entity an entity to remove
     * @return true if removed successfully false if not exists
     * @throws RepoAccessException When the repo is not accessible
     */
    boolean remove(T entity) throws RepoAccessException;

    /**
     * informing the Subscriber that changes in Cache occurred
     *
     * @param operation operation performed on cache
     * @param entity    an entity modified
     */
    void inform(Cache.Operation operation, T entity);
}
