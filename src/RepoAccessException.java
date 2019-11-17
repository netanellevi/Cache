/**
 * Represents an exception in accessing info in repository
 */
class RepoAccessException extends Exception {
    RepoAccessException() {
        super("Repository of Cache is not accessible and can't be attached.");
    }

    RepoAccessException(int eId) {
        super("Repository of Cache is not accessible. Entity " + eId + " not modified.");
    }
}
