package org.example;
import java.util.*;

abstract class vehicle{
    String plate_number;
    Date date ;
    int speed;
    boolean seatbelt ;

    public vehicle(String plate_number, int speed, boolean seatbelt) {
        this.plate_number = plate_number;
        this.speed = speed;
        this.seatbelt = seatbelt;
        this.date = new Date();
    }

    abstract int getMAXSpeed();
}


class Truck extends vehicle{
    int Max_speed = 60 ;

    public Truck(String plate_number, int speed , boolean seatbelt) {
        super(plate_number, speed, seatbelt);
    }

    @Override
    int getMAXSpeed() {
        return this.Max_speed;
    }
}

class Private extends vehicle{
    int Max_speed = 80 ;

    public Private(String plate_number, int speed , boolean seatbelt) {
        super(plate_number, speed, seatbelt);
    }

    @Override
    int getMAXSpeed() {
        return this.Max_speed;
    }
}


class observation{
    int fine ;
    String plate_number ;
    String type ;
    String print ;

    public observation(int fine,String plate_number,String type, String print) {
        this.fine = fine;
        this.plate_number = plate_number;
        this.type = type;
        this.print = print + fine +" EGP";
    }
}


interface Rule{

    observation checkViolation(vehicle v);

}


class SpeedRule implements Rule{

    @Override
    public observation checkViolation(vehicle v) {
        if(v.speed>v.getMAXSpeed()){
            return new observation(300,v.plate_number,"Speed","speed of "+v.speed+" exceeded max allowed "+v.getMAXSpeed()+" : " );
        }
        return null;
    }
}

class SeatbeltRule implements Rule{

    @Override
    public observation checkViolation(vehicle v) {
        if(!v.seatbelt)return new observation(100,v.plate_number,"Seatbelt","Seatbelt not fastned : ");
        return null;
    }
}

class MinSpeedRule implements Rule{
    final int Minspeed = 20 ;
    @Override
    public observation checkViolation(vehicle v) {
        if (v.speed < this.Minspeed)
            return new observation(50, v.plate_number, "Slow_Speed", "speed of " + v.speed + " Too Slow allowed " + this.Minspeed + " : ");
        return null;
    }
}


class QuRadar{
    HashMap<String, Integer> fines = new HashMap<>();
    HashMap<String, List<observation>> observations = new HashMap<>();
    HashMap<String, Integer> violated = new HashMap<>();
    List<Rule> Rules = new ArrayList<>();

    void add_Rule(Rule rule){
        Rules.add(rule);
    }

    void Check_vehicle(vehicle vehicle){
        HashMap<String, List<observation>> obs_temp = new HashMap<>();
        int temp_fines = 0 ;
        for (Rule r : Rules){
            observation temp = r.checkViolation(vehicle);

            if(temp!=null){
                fines.put(temp.plate_number,fines.getOrDefault(temp.plate_number,0)+temp.fine);
                temp_fines+=temp.fine;
                List<observation> list = observations.getOrDefault(temp.plate_number, new ArrayList<>());
                list.add(temp);
                observations.put(temp.plate_number, list);
                violated.put(temp.type,violated.getOrDefault(temp.type,0)+1);

                List<observation> list_t = obs_temp.getOrDefault(temp.plate_number, new ArrayList<>());
                list_t.add(temp);
                obs_temp.put(temp.plate_number, list_t);

            }

        }
        if(obs_temp.containsKey(vehicle.plate_number)){
            System.out.println("Traffic for car "+ vehicle.plate_number);
            System.out.println("Total amount: "+temp_fines+" EGP");
            System.out.println("Violations:");
            for(observation ob : obs_temp.get(vehicle.plate_number)){
                System.out.println("- " + ob.print);
            }
        }
    }
    void get_violated(){
        for (String ruleName : violated.keySet()) {
            System.out.println(ruleName +" Have been violated :- "+violated.get(ruleName));
        }
    }
    void get_fines(){
        for(String plate : fines.keySet()){
            System.out.println("Vehicle with plate number :- " +plate+ " has total fines = " +fines.get(plate)+" EGP");
        }
    }
}

class Main {
    public static void main(String[] args) {

        // Set up the radar with the rules we want to check
        QuRadar radar = new QuRadar();
        radar.add_Rule(new SeatbeltRule());
        radar.add_Rule(new SpeedRule());
        radar.add_Rule(new MinSpeedRule());

        // Speeding + no seatbelt -> should get fined for both (400 EGP)
        System.out.println("--- Test 1: private car, speed 102, no seatbelt ---");
        radar.Check_vehicle(new Private("ABC1234", 102, false));
        System.out.println();

        // Same car checked again later -> should be its own separate fine, not stacked with Test 1
        System.out.println("--- Test 2: same car checked AGAIN with same violation ---");
        radar.Check_vehicle(new Private("ABC1234", 170, false));
        System.out.println();

        // A car following all the rules -> should print nothing
        System.out.println("--- Test 3: compliant private car ---");
        radar.Check_vehicle(new Private("XYZ999", 70, true));
        System.out.println();

        // Different vehicle type -> should use the truck speed limit, not the private car one
        System.out.println("--- Test 4: speeding truck ---");
        radar.Check_vehicle(new Truck("TRK555", 75, true));

        // A car driving too slowly -> should trigger MinSpeedRule (50 EGP)
        System.out.println("\n--- Test 5: private car driving too slowly (speed 10) ---");
        radar.Check_vehicle(new Private("SLW111", 10, true));


        // Totals across every check we did above
        System.out.println("===================================");
        radar.get_violated();
        radar.get_fines();
    }
}