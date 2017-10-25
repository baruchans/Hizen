package net.baruchans.hizen;

public enum Meal {
    NULL("null"),
    BEER("ビール"),
    KARAAGE("唐揚げ");

    private String name;

    Meal(String name) {
        this.name = name;
    }

    public static Meal convertToMeal(String name) {
        for (Meal meal : values()) {
            if (meal.name.equals(name)) {
                return meal;
            }
        }

        return Meal.NULL;
    }

}
