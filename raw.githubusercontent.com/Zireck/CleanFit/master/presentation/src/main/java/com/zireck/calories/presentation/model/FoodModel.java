package com.zireck.calories.presentation.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class that represents a food in the presentation layer.
 */
public class FoodModel implements Parcelable {

    private long id;
    private String name;
    private String brand;
    private boolean isDrink;
    private double calories;
    private double fats;
    private double carbohydrates;
    private double proteins;
    private String picture;

    public FoodModel() {

    }

    public FoodModel(long id) {
        this.id = id;
    }

    protected FoodModel(Parcel in) {
        id = in.readLong();
        name = in.readString();
        brand = in.readString();
        isDrink = in.readByte() != 0x00;
        calories = in.readDouble();
        fats = in.readDouble();
        carbohydrates = in.readDouble();
        proteins = in.readDouble();
        picture = in.readString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public boolean isDrink() {
        return isDrink;
    }

    public void setIsDrink(boolean isDrink) {
        this.isDrink = isDrink;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getFats() {
        return fats;
    }

    public void setFats(double fats) {
        this.fats = fats;
    }

    public double getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public double getProteins() {
        return proteins;
    }

    public void setProteins(double proteins) {
        this.proteins = proteins;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("***** Food Entity Details *****\n");
        stringBuilder.append("id=" + String.valueOf(this.getId()) + "\n");
        stringBuilder.append("name=" + this.getName() + "\n");
        stringBuilder.append("brand=" + this.getBrand() + "\n");
        stringBuilder.append("isDrink=" + String.valueOf(this.isDrink()) + "\n");
        stringBuilder.append("calories=" + String.valueOf(this.getCalories()) + "\n");
        stringBuilder.append("fats=" + String.valueOf(this.getFats()) + "\n");
        stringBuilder.append("carbohydrates=" + String.valueOf(this.getCarbohydrates()) + "\n");
        stringBuilder.append("proteins=" + String.valueOf(this.getProteins()) + "\n");
        stringBuilder.append("*******************************");

        return stringBuilder.toString();
    }

    public double getFatsPercent() {
        return getNutrientPercent(this.getFats());
    }

    public double getCarbohydratesPercent() {
        return getNutrientPercent(this.getCarbohydrates());
    }

    public double getProteinsPercent() {
        return getNutrientPercent(this.getProteins());
    }

    /**
     * Calculate percentage for a certain nutrient
     *
     * @param nutrient Nutrient to calculate
     * @return Percentage
     */
    private double getNutrientPercent(double nutrient) {
        if (nutrient <= 0) {
            return 0;
        }

        double nutrientsTotalAmount = this.getFats() + this.getCarbohydrates() + this.getProteins();
        return (nutrient * 100) / nutrientsTotalAmount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(brand);
        dest.writeByte((byte) (isDrink ? 0x01 : 0x00));
        dest.writeDouble(calories);
        dest.writeDouble(fats);
        dest.writeDouble(carbohydrates);
        dest.writeDouble(proteins);
        dest.writeString(picture);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FoodModel> CREATOR = new Parcelable.Creator<FoodModel>() {
        @Override
        public FoodModel createFromParcel(Parcel in) {
            return new FoodModel(in);
        }

        @Override
        public FoodModel[] newArray(int size) {
            return new FoodModel[size];
        }
    };

}
