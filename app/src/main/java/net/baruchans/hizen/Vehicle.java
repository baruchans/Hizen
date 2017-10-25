package net.baruchans.hizen;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import jp.novars.mabeee.sdk.App;
import jp.novars.mabeee.sdk.Device;

public class Vehicle {
    private static final String TAG = Vehicle.class.getSimpleName();

    private Device mabeee;

    private int start;
    private int goal;
    private int current;

    private Section section;

    private Queue<Integer> route;

    private Meal meal;
    private MealStatus mealStatus;

    private float pressure;

    public Vehicle(Device mabeee, int initPoint) {
        this.mabeee = mabeee;
        this.current = initPoint;

        meal = Meal.NULL;
        mealStatus = MealStatus.EMPTY;
        route = new LinkedList<>();

        connect();

        // fixme: for demo
        initializeForDemo();
    }

    private void initializeForDemo() {
        // fixme: for demo
        String mabeeeName = mabeee.getName();

        switch (mabeeeName) {
            case "MaBeee015453": {
                setCurrent(0);
                setMealStatus(MealStatus.EMPTY);
                break;
            }
            case "MaBeee015458": {
                setCurrent(1);
                setMealStatus(MealStatus.EMPTY);
                break;
            }
            case "MaBeee015459": {
                setCurrent(2);
                setMeal(Meal.BEER);
                setMealStatus(MealStatus.REMAIN);
                break;
            }
            default: {
                break;
            }
        }
    }

    public void connect() {
        if (mabeee.getState() == Device.State.Disconnected) {
            Log.d(TAG, mabeee.getName() + " : connect");
            App.getInstance().connect(mabeee);
        }
    }

    public boolean hasMeal(Meal meal) {
        return ((this.meal == meal) && ( ! (mealStatus == MealStatus.EMPTY)));
    }

    public int getCurrent() {
        return current;
    }

    public int getGoal() {
        return goal;
    }

    public void setRoute(Queue<Integer> route) {
        this.route = route;

        section = new Section(current, route.peek());
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public void run() {
        mabeee.setPwmDuty(70);
    }

    public boolean isNext(int point) {
        return ((route.peek() == point) && (section.getEnd() == point));
    }

    public void pass() {
        current = route.poll();

        if (route.size() > 0) {
            section = new Section(current, route.peek());
        } else {
            // fixme: goal+1
            section = new Section(current, goal);
        }
    }

    public void stop() {
        mabeee.setPwmDuty(0);
    }

    public boolean isGoal() {
        return (route.isEmpty());
    }

    public boolean hasMabeee(Device device) {
        return (mabeee.getIdentifier() == device.getIdentifier());
    }

    public boolean becomeLight(float pressure) {
        return ((this.pressure - pressure) > 150);
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public String getName() {
        return mabeee.getName();
    }

    public int getGoalDistance() {
        return route.size();
    }

    /**
     * for demo
     */
    public void setCurrent(int current) {
        this.current = current;
    }

    /**
     * for demo
     */
    public void setMeal(Meal meal) {
        this.meal = meal;
    }

    /**
     * for demo
     * @param mealStatus
     */
    public void setMealStatus(MealStatus mealStatus) {
        this.mealStatus = mealStatus;
    }
}
