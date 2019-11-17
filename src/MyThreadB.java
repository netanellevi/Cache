/**
 * A Thread for testing purpose
 */
public class MyThreadB extends Thread {
    private Subscriber<Dog> subscriber;
    private boolean pass;

    MyThreadB(Subscriber<Dog> subscriber) {
        this.subscriber = subscriber;
        this.pass = true;
    }

    @Override
    public void run() {
        try {
            // This thread intend to run slower than the other (-MyThreadA)
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Dog d = this.subscriber.get(0);
        //ensure he gets the updated entity that MyThreadA performs before
        if (d.equals(new Dog(0, "old", 10, 20, "a"))) {
            this.pass = false;
        }
    }

    boolean isPassed() {
        return pass;
    }
}
