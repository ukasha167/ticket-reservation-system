
import java.io.Serializable;

public class Seat implements Serializable {
    String id;
    boolean booked;
    String name;
    String cnic;
    String phone;

    public Seat(String id) {
        this.id = id;
        this.booked = false;
        this.name = "";
        this.cnic = "";
        this.phone = "";
    }

    public Seat(Seat other) {
        this.id = other.id;
        this.booked = other.booked;
        this.name = other.name;
        this.cnic = other.cnic;
        this.phone = other.phone;
    }
}