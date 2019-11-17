/**
 * A Thread for testing purpose
 */
public class MyThreadA extends Thread {
    private Subscriber<Dog> subscriber;

    MyThreadA(Subscriber<Dog> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void run() {
        try {
            // This thread intend to run faster than the other (-MyThreadB)
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            this.subscriber.update(new Dog(0, "new", 20, 30, "b"));
        } catch (RepoAccessException e) {
            e.printStackTrace();
        }
    }
}
