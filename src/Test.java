import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * A Unit test for the library
 */
public class Test {
    private static final String basicDB = "db-basic";
    private static final String persistentDB = "db-persistent";
    private static final String readOnlyDB = "db-readOnly";

    /**
     * Resetting the repo to ensure clean testing
     */
    private static void resetRepo() {
        PrintWriter pw;
        try {
            pw = new PrintWriter(basicDB);
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static boolean addTest(Cache.Mode mode) throws RepoAccessException {
        resetRepo();
        Dog dog0 = new Dog(0, "Flaky", 100, 2, "Labrador");
        Dog dog1 = new Dog(1, "Flay", 10, 20, "Pincher");
        Cache<Dog> c;
        Subscriber<Dog> a;
        Subscriber<Dog> b;
        c = new Cache<>(new DemoRepo(basicDB), mode, new DogConverter());
        a = new User<>("User A", c);
        b = new User<>("User B", c);
        if (!a.add(dog0)) return false;
        if (a.get(dog0.getId()) == null) return false;
        if (b.get(dog0.getId()) == null) return false; // check that b got what a inserted
        if (!b.add(dog1)) return false;
        if (a.get(dog1.getId()) == null) return false;

        // duplicate testing
        if (a.add(dog0)) return false;
        if (b.add(dog1)) return false;        // indicate success in adding duplicate objects
        c.unregister(a);
        c.unregister(b);
        return true;
    }

    private static boolean removeTest(Cache.Mode mode) throws RepoAccessException {
        resetRepo();
        // Test remove null
        Cache<Dog> c;
        Subscriber<Dog> a;
        Subscriber<Dog> b;
        c = new Cache<>(new DemoRepo(basicDB), mode, new DogConverter());
        a = new User<>("User A", c);
        b = new User<>("User B", c);
        Dog dog = new Dog(0, "Flaky", 100, 20, "Labrador");
        if (a.remove(dog)) return false;
        if (b.remove(dog)) return false;// indicate success in removing while the object not really there
        // Test remove something
        c = new Cache<>(new DemoRepo(basicDB), mode, new DogConverter());
        dog = new Dog(0, "Flaky", 100, 20, "Labrador");
        a = new User<>("User A", c);
        b = new User<>("User B", c);
        if (!a.add(dog)) return false;
        if (!b.remove(dog)) return false;
        if (a.get(0) != null) return false;
        return b.get(0) == null;
    }

    private static boolean updateLazyTest() throws RepoAccessException {
        //test updating something not in cache but in repo
        Cache<Dog> c = new Cache<>(new DemoRepo(persistentDB), Cache.Mode.LAZY, new DogConverter());
        return c.update(new Dog(0, "Starky", 10, 35, "Amstaf"));
    }

    private static boolean updateTest(Cache.Mode mode) throws RepoAccessException {
        resetRepo();
        Cache<Dog> c;
        Subscriber<Dog> a;
        Subscriber<Dog> b;
        // test updating nothing
        Dog dog = new Dog(2, "Starky", 10, 35, "Amstaf");
        c = new Cache<>(new DemoRepo(basicDB), mode, new DogConverter());
        a = new User<>("User A", c);
        b = new User<>("User B", c);
        if (a.update(dog)) return false;
        if (b.update(dog)) return false;
        // test updating something
        c = new Cache<>(new DemoRepo(basicDB), mode, new DogConverter());
        a = new User<>("User A", c);
        b = new User<>("User B", c);
        if (!a.add(dog)) return false;
        dog = new Dog(dog.getId(), "My", 20, 50, "MyRace");
        if (!b.update(dog)) return false; // updating not occurred
        return dog.equals(c.get(dog.getId())); // updating not performed correctly
    }

    private static boolean getTest(Cache.Mode mode) throws RepoAccessException {
        resetRepo();
        // test get nothing
        Cache<Dog> c;
        Subscriber<Dog> a;
        Subscriber<Dog> b;
        c = new Cache<>(new DemoRepo(basicDB), mode, new DogConverter());
        a = new User<>("User A", c);
        b = new User<>("User B", c);
        if (a.get(0) != null) return false;
        if (b.get(0) != null) return false;
        // test get something
        c = new Cache<>(new DemoRepo(basicDB), mode, new DogConverter());
        a = new User<>("User A", c);
        b = new User<>("User B", c);
        Dog dog = new Dog(2, "Starky", 10, 35, "Amstaf");
        if (!a.add(dog)) return false;
        return b.get(dog.getId()) != null;
    }

    //Test that an existing repo (full with entries) functionality
    private static boolean persistentDBTest(Cache.Mode mode) throws RepoAccessException {
        DemoRepo repo = new DemoRepo(persistentDB);
        Cache<Dog> c;
        Subscriber<Dog> a;
        Subscriber<Dog> b;
        c = new Cache<>(repo, mode, new DogConverter());
        a = new User<>("User A", c);
        b = new User<>("User B", c);
        if (a.get(0) == null) return false;
        if (b.get(0) == null) return false;
        Dog dog = new Dog(2, "Starky", 10, 35, "Amstaf");
        a = new User<>("User A", c);
        b = new User<>("User B", c);
        if (a.add((dog))) return false;
        if (b.add((dog))) return false;
        int id = repo.getMaxId() + 1;
        dog = new Dog(id, "Starky", 10, 35, "Amstaf");
        if (!a.add(dog)) return false;
        dog = new Dog(dog.getId(), "My", 20, 40, "MyRace");
        if (!b.update(dog)) return false;
        return dog.equals(c.get(dog.getId()));
    }

    private static boolean consistentTest(Cache.Mode mode) throws RepoAccessException {
        boolean flag = true;
        File f = new File(readOnlyDB);
        if (!f.setWritable(true)) {
            System.out.println("Consistent Test CORRUPTED!!!"); // corner case when the file of the test corrupted
        }
        Cache<Dog> c;
        Subscriber<Dog> a;
        Subscriber<Dog> b;
        Dog dog;
        c = new Cache<>(new DemoRepo(readOnlyDB), mode, new DogConverter());
        b = new User<>("User B", c);
        a = new User<>("User A", c);
        if (!f.setReadOnly()) {
            System.out.println("Consistent Test CORRUPTED!!!"); // corner case when the file of the test corrupted
        }
        dog = a.get(0);
        try {
            if (b.update(new Dog(dog.getId(), "My", 5, 5, "My")))
                flag = false;
        } catch (RepoAccessException ignored) {
            // exception is expected to be thrown because of the file is not writable
        }
        if (!dog.equals(c.get(0))) flag = false; // ensures that the update DOES NOT performed on cache
        if (!f.setWritable(true)) {
            System.out.println("Consistent Test CORRUPTED!!!"); // corner case when the file of the test corrupted
        }
        return flag;
    }

    private static boolean threadSafeTest(Cache.Mode mode) throws RepoAccessException {
        resetRepo();
        Cache<Dog> c = new Cache<>(new DemoRepo(basicDB), mode, new DogConverter());
        Subscriber<Dog> a = new User<>("User A", c);
        Subscriber<Dog> b = new User<>("User B", c);
        // User A inserting an OLD dog
        if (!a.add(new Dog(0, "old", 10, 20, "a"))) return false;
        MyThreadA threadA = new MyThreadA(a);
        MyThreadB threadB = new MyThreadB(b);
        threadA.start(); // User A updating cached dog to NEW in 10ms
        threadB.start(); // User B get cached dog in 100ms --- ENSURES HE GETS THE UPDATED ONE (By isPassed method)!!
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return threadB.isPassed();
    }

    public static void main(String[] args) {
        try {
            System.out.println(removeTest(Cache.Mode.LAZY) && removeTest(Cache.Mode.EAGER) ? "Removing PASSED" : "Removing FAILED");
            System.out.println(getTest(Cache.Mode.LAZY) && getTest(Cache.Mode.EAGER) ? "Getting PASSED" : "Getting FAILED");
            System.out.println(addTest(Cache.Mode.LAZY) && addTest(Cache.Mode.EAGER) ? "Adding PASSED" : "Adding FAILED");
            System.out.println(updateTest(Cache.Mode.LAZY) && updateTest(Cache.Mode.EAGER) ? "Updating PASSED" : "Updating FAILED");
            System.out.println(persistentDBTest(Cache.Mode.LAZY) && persistentDBTest(Cache.Mode.EAGER) ? "Persistent PASSED" : "Persistent FAILED");
            System.out.println(consistentTest(Cache.Mode.LAZY) && consistentTest(Cache.Mode.EAGER) ? "Consistent PASSED" : "Consistent FAILED");
            System.out.println(threadSafeTest(Cache.Mode.LAZY) && threadSafeTest(Cache.Mode.EAGER) ? "ThreadSafe PASSED" : "ThreadSafe FAILED");
            System.out.println(updateLazyTest() ? "UpdateLazy PASSED" : "UpdateLazy FAILED");
        } catch (RepoAccessException e) {
            System.err.println("Tests corrupted");
        }
    }


}
