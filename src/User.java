
/**
 * A Subscriber that demonstrate the library functionality
 */
public class User<T extends Entity> implements Subscriber<T> {
    private String name;
    private Cache<T> c;

    User(String name, Cache<T> c) {
        this.name = name;
        this.c = c;
        c.register(this);
    }

    @Override
    public T get(Integer eId) {
        return c.get(eId);
    }

    @Override
    public boolean add(T entity) throws RepoAccessException {
        return c.add(entity);
    }

    @Override
    public boolean update(T entity) throws RepoAccessException {
        return c.update(entity);
    }

    @Override
    public boolean remove(T entity) throws RepoAccessException {
        return c.remove(entity);
    }

    @Override
    public void inform(Cache.Operation operation, T entity) {
        // Basically for testing purpose
        String opS = null;
        switch (operation) {
            case ADD:
                opS = "ADDED";
                break;
            case UPDATE:
                opS = "UPDATED";
                break;
            case REMOVE:
                opS = "REMOVED";
                break;
        }
        System.out.println("(" + name + "):" + "Entity " + entity.getId() + " " + opS);
    }
}
