package com.future.lock;/** * @Description: * @Author: lilei58 * @Date: Created in 2022/1/5 上午7:12 */public class Order {    public void createOrder() {        System.out.println(Thread.currentThread().getName() + " 创建 Order");    }}