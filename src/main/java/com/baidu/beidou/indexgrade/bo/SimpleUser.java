package com.baidu.beidou.indexgrade.bo;

import java.util.Calendar;
import java.util.Date;

public class SimpleUser {

    private static Calendar calendar = Calendar.getInstance();

    static {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
    }

    private int userId;
    private int cost;
    private Date day;

    private int yestodayCost;
    private int weekSumCost;
    private int monthSumCost;

    public SimpleUser() {
    }

    public SimpleUser(int userId, int cost, Date day) {
        super();
        this.userId = userId;
        this.cost = cost;
        this.day = day;
    }

    public void addYestodayCost(int cost) {
        this.yestodayCost += cost;
    }

    public void addWeekCost(int cost) {
        this.weekSumCost += cost;
    }

    public void addMonthCost(int cost) {
        this.monthSumCost += cost;
    }

    public void calculatePowerConsume() {
        this.cost = ((monthSumCost / 30) + (weekSumCost) + (yestodayCost * 2)) / 10;
    }

    public void addConsume(SimpleUser user) {
        if (isDateInDistance(user.getDay(), calendar.getTime(), timeMillisOneDay)) {
            this.yestodayCost += user.getCost();
        }
        if (isDateInDistance(user.getDay(), calendar.getTime(), timeMillisOneWeek)) {
            this.weekSumCost += user.getCost();
        }
        if (isDateInDistance(user.getDay(), calendar.getTime(), timeMillisOneMonth)) {
            this.monthSumCost += user.getCost();
        }
    }

    private boolean isDateInDistance(Date consumeDate, Date today, long distance) {

        long consumeTime = consumeDate.getTime();
        long todayTime = today.getTime();

        if ((todayTime - consumeTime) <= distance) {
            return true;
        }

        return false;
    }

    /**
     * @return the userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @return the cost
     */
    public int getCost() {
        return cost;
    }

    /**
     * @param cost the cost to set
     */
    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     * @return the day
     */
    public Date getDay() {
        return day;
    }

    /**
     * @param day the day to set
     */
    public void setDay(Date day) {
        this.day = day;
    }

    /**
     * @return the yestodayCost
     */
    public int getYestodayCost() {
        return yestodayCost;
    }

    /**
     * @param yestodayCost the yestodayCost to set
     */
    public void setYestodayCost(int yestodayCost) {
        this.yestodayCost = yestodayCost;
    }

    /**
     * @return the weekSumCost
     */
    public int getWeekSumCost() {
        return weekSumCost;
    }

    /**
     * @param weekSumCost the weekSumCost to set
     */
    public void setWeekSumCost(int weekSumCost) {
        this.weekSumCost = weekSumCost;
    }

    /**
     * @return the monthSumCost
     */
    public int getMonthSumCost() {
        return monthSumCost;
    }

    /**
     * @param monthSumCost the monthSumCost to set
     */
    public void setMonthSumCost(int monthSumCost) {
        this.monthSumCost = monthSumCost;
    }

    private final long timeMillisOneDay = 86400L * 1000;
    private final long timeMillisOneWeek = 86400L * 1000 * 7;
    private final long timeMillisOneMonth = 86400L * 1000 * 30;

}
