/**
 * An entity that demonstrate the library functionality
 */
public class Dog implements Entity {
    private int id;
    private String name;
    private int height;
    private int weight;
    private String race;

    public Dog() {
    }

    public Dog(int id, String name, int height, int weight, String race) {
        this.id = id;
        this.name = name;
        this.height = height;
        this.weight = weight;
        this.race = race;
    }

    public String getName() {
        return name;
    }

    //
////    public void setName(String name) {
////        this.name = name;
////    }
//
    public int getHeight() {
        return height;
    }

    //
////    public void setHeight(int height) {
////        this.height = height;
////    }
//
    public int getWeight() {
        return weight;
    }

    //
////    public void setWeight(int weight) {
////        this.weight = weight;
////    }
//
    public String getRace() {
        return race;
    }

    //
////    public void setRace(String race) {
////        this.race = race;
////    }
//


    // for testing purpose
    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        if (!(o instanceof Dog)) {
            return false;
        }
        return (id == ((Dog) o).id) && name.equals(((Dog) o).name) && (height == ((Dog) o).height) &&
                (weight == ((Dog) o).weight) && (race.equals(((Dog) o).race));
    }


    @Override
    public int getId() {
        return this.id;
    }
//    @Override
//    public Dog clone(){
//        return new Dog(this);
//    }
}
