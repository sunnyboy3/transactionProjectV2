package org.jk.entity;

/**
 * @ClassName Order
 * @Description 订单对象
 * @Author wp
 * @Date 2022/10/18 10:16
 **/
public class Order {
    private String orderId;
    private double amount;
    private int flag = 0;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
