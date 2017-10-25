package net.baruchans.hizen;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import jp.novars.mabeee.sdk.App;
import jp.novars.mabeee.sdk.Device;

public class HizenApplication {
    private static final String TAG = HizenApplication.class.getSimpleName();

    private AndroidThingsDevice device;
    private ArrayList<Vehicle> vehicles;
    private boolean isServeMode = false;

    private int SENSOR_COUNT;

    private ArrayList<Integer> goaledPoints;

    private ArrayList<JuliusClient> juliusClients;

    private static HizenApplication app;

    public HizenApplication() {
        HizenApplication.app = this;
    }

    public void run() {
        device = new AndroidThingsDevice(HizenActivity.getContext());

        initialize();
    }

    /**
     *  (channel = position)
     * @param pressure
     * @param channel
     */
    public void updatePressure(int channel, String state, float pressure) {
        if (isServeMode) {

            Log.i(this.getClass().getName(), channel + ", pressure: " + pressure);

            for (Integer goaledPoint : goaledPoints) {
                if (channel == goaledPoint) {
                    return ;
                }
            }

            ArrayList<Vehicle> candidateVehicles = new ArrayList<>();
            for (Vehicle vehicle : vehicles) {
                if ( ! vehicle.isGoal()) {
                    if (vehicle.isNext(channel)) {
                        candidateVehicles.add(vehicle);
                    }
                }
            }

            if (candidateVehicles.size() >= 1) {
                int distance = 999;
                Vehicle targetVehicle = null;
                for (Vehicle vehicle : candidateVehicles) {
                    if (distance > vehicle.getGoalDistance()) {
                        distance = vehicle.getGoalDistance();
                        targetVehicle = vehicle;
                    }
                }

                targetVehicle.pass();

                if (targetVehicle.isGoal()) {
                    targetVehicle.stop();
                    targetVehicle.setPressure(pressure);
                    goaledPoints.add(channel);
                }

                return ;
            }

            boolean isAllVehiclesGoaled = true;
            for (Vehicle vehicle : vehicles) {
                if ( ! vehicle.isGoal()) {
                    isAllVehiclesGoaled = false;
                }
            }

            isServeMode = ( ! isAllVehiclesGoaled);
        } else if (state.equals("leave")) { // waiting
            /*
            // get vehicle on the point
            Vehicle targetVehicle = null;
            for (Vehicle vehicle : vehicles) {
                if (vehicle.getCurrent() == channel) {
                    targetVehicle = vehicle;
                }
            }

            // 空の皿が乗った時だけいなくなる
            // 皿がとられた
            // goal settings
            int startPosition = targetVehicle.getCurrent();
            int goalPosition = SENSOR_COUNT - 2;
            targetVehicle.setGoal(-1); // -1: 下膳先

            // case: start=1, goal=3
            ArrayList<Vehicle> targetVehicles = new ArrayList<>();
            if (startPosition < goalPosition) {
                for (Vehicle vehicle : vehicles) {
                    int current = vehicle.getCurrent();
                    if ((current >= startPosition) && (current <= goalPosition)) {
                        targetVehicles.add(vehicle);
                    }
                }
            } else { // case: start=3, goal=1
                for (Vehicle vehicle : vehicles) {
                    int current = vehicle.getCurrent();
                    if ((current >= startPosition) || (current <= goalPosition)) {
                        targetVehicles.add(vehicle);
                    }
                }
            }


            // set goal
            for (Vehicle vehicle : targetVehicles) {
                if ( ! vehicle.equals(targetVehicle)) {
                    int current = vehicle.getCurrent();
                    int count   = 0;
                    for (int i=startPosition; (i % SENSOR_COUNT) != current; ++i) {
                        for (Vehicle vehicle1 : targetVehicles) {
                            if (vehicle1.getCurrent() == i) {
                                count++;
                            }
                        }

                        vehicle.setGoal((goalPosition + count) % SENSOR_COUNT);
                    }
                }

                // set route
                for (Vehicle vehicle : targetVehicles) {
                    Queue<Integer> route = new LinkedList<>();
                    int goal = vehicle.getGoal();
                    for (int i=vehicle.getCurrent()+1; (i % SENSOR_COUNT) != goal; ++i) {
                        route.offer(i % SENSOR_COUNT);
                    }
                    route.offer(goal);

                    vehicle.setRoute(route);
                }

                for (Vehicle vehicle : targetVehicles) {
                    Log.i(this.getClass().getName(), vehicle.getName() + " : " + vehicle.getGoal());
                    vehicle.run();
                }
            }

*/

        }
    }

    public void updateVoice(String voice, int port) {
        // todo: fixme

        isServeMode  = true;
        goaledPoints = new ArrayList<>();

        Meal inputMeal = Meal.convertToMeal(voice);

        //int goalPosition  = port - JULIUS_PORT_BASE + 1; // for demo
        int goalPosition  = port - 1; // for demo

        Vehicle targetVehicle = null;
        for (Vehicle vehicle : vehicles) {
            if (vehicle.hasMeal(inputMeal)) {
                // todo: 唐揚げが複数あったら近い方がくる？
                targetVehicle = vehicle;
                targetVehicle.setGoal(goalPosition);
            }
        }

        if (targetVehicle == null) {
            // todo: 何かする？注文を促すなど。
            return ;
        }

        // goal settings
        int startPosition = targetVehicle.getCurrent();

        // example case: start=1, goal=3
        ArrayList<Vehicle> targetVehicles = new ArrayList<>();
        if (startPosition < goalPosition) {
            for (Vehicle vehicle : vehicles) {
                int current = vehicle.getCurrent();
                if ((current >= startPosition) && (current <= goalPosition)) {
                    targetVehicles.add(vehicle);
                }
            }
        } else { // example case: start=3, goal=1
            for (Vehicle vehicle : vehicles) {
                int current = vehicle.getCurrent();
                if ((current >= startPosition) || (current <= goalPosition)) {
                    targetVehicles.add(vehicle);
                }
            }
        }

        // set goal
        for (Vehicle vehicle : targetVehicles) {
            if ( ! vehicle.equals(targetVehicle)) {
                int current = vehicle.getCurrent();
                int count   = 0;
                for (int i=startPosition; (i % SENSOR_COUNT) != current; ++i) {
                    for (Vehicle vehicle1 : targetVehicles) {
                        if (vehicle1.getCurrent() == i) {
                            count++;
                        }
                    }

                    vehicle.setGoal((goalPosition + count) % SENSOR_COUNT);
                }
            }
        }

        // set route
        for (Vehicle vehicle : targetVehicles) {
            Queue<Integer> route = new LinkedList<>();
            int goal = vehicle.getGoal();
            for (int i=vehicle.getCurrent()+1; (i % SENSOR_COUNT) != goal; ++i) {
                route.offer(i % SENSOR_COUNT);
            }
            route.offer(goal);

            vehicle.setRoute(route);
        }

        for (Vehicle vehicle : targetVehicles) {
            Log.i(this.getClass().getName(), vehicle.getName() + " : " + vehicle.getGoal());
            vehicle.run();
        }
    }

    /**
     * initialize Mabeee SDK and scan & connect all Mabeees.
     */
    private void initialize() {
        vehicles = new ArrayList<>();

        Context context = HizenActivity.getContext();
        PreferenceManager.setDefaultValues(context, R.xml.preference, true);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String juliusHost = preferences.getString(context.getString(R.string.julius_host_key), null);
        int juliusPort = Integer.parseInt(preferences.getString(context.getString(R.string.julius_port_key), null));
        int juliusCount = Integer.parseInt(preferences.getString(context.getString(R.string.julius_count_key), null));

        juliusClients = new ArrayList<>();
        for (int i=0; i < juliusCount; i++) {
            JuliusClient juliusClient = new JuliusClient(juliusHost, juliusPort + i);
            juliusClient.connect();

            juliusClients.add(juliusClient);
        }


        String sensorHost = preferences.getString(context.getString(R.string.sensor_host_key), null);
        int sensorPort = Integer.parseInt(preferences.getString(context.getString(R.string.sensor_port_key), null));
        SENSOR_COUNT = Integer.parseInt(preferences.getString(context.getString(R.string.sensor_count_key), null));

        SensorClient sensorSocket = new SensorClient(sensorHost, sensorPort);
        sensorSocket.connect(this);

        initializeMabeee();
        scanMabeee();
    }

    /**
     * initialize Mabeee SDK
     */
    private void initializeMabeee() {
        Log.d(TAG, "***** init Mabeee *****");

        App.getInstance().initializeApp(HizenActivity.getContext());
    }

    private void scanMabeee() {
        Log.d(TAG, "***** start scan Mabeee *****");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        App.getInstance().startScan(new App.ScanListener() {
            @Override
            public void didUpdateDevices(Device[] devices) {
                // If no mabeees were founded, do nothing.
                if (hasNotAnyMabeees(devices)) {
                    return;
                }

                for (Device device : devices) {
                    Log.d(TAG, device.getName());

                    // if the device is already connected, process next device.
                    if ( ! isConnected(device)) {
                        Vehicle vehicle = new Vehicle(device, 0);
                        vehicles.add(vehicle);
                    }
                }

                if (isAllDevicesConnected()) {
                    Log.d(TAG, "***** stop scan Mabeee *****");
                    App.getInstance().stopScan();
                }
            }

            private boolean hasNotAnyMabeees(Device[] device) {
                return (device.length <= 0);
            }

            private boolean isConnected(Device device) {
                for (Vehicle vehicle : vehicles) {
                    if (vehicle.hasMabeee(device)) {
                        return true;
                    }
                }

                return false;
            }

            private boolean isAllDevicesConnected() {
                for (Device device : App.getInstance().getDevices()) {
                    if ( ! (device.getState() == Device.State.Connected)) {
                        return false;
                    }
                }

                return true;
            }
        });

    }

    public static HizenApplication getApp() {
        return HizenApplication.app;
    }
}
