package com.future.zookeeper;import org.apache.zookeeper.*;import org.apache.zookeeper.data.ACL;import org.apache.zookeeper.data.Stat;import java.io.IOException;/** * @Description: * @Author: lilei58 * @Date: Created in 2021/11/23 上午7:13 */public class ZookeeperClientTest {    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {        // 默认的 Watch        ZooKeeper client = new ZooKeeper("localhost:2181", 5000, new Watcher() {            @Override            public void process(WatchedEvent event) {                System.out.println("连接的时候" + event);            }        });        Stat stat = new Stat();        /**         * 可以注册 Watcher 的方法: getData, exists, getChildren。         * 可以触发 Watcher 的方法: create, delete, setData, 连接断开的情况下触发的 watcher 会丢失。         *         * 问题: getData() 方法中找不到 Stat stat         * 原因: 因为 Maven 依赖未正常加载         * 解决: 重新 compile Jute 包即可         *         * 注意: Watcher 没有发送给服务端,仅仅只是判断了是否为空         * request.setWatch(watcher != null);         */        String str = String.valueOf(client.getData("/firstNode", new Watcher() {            @Override            public void process(WatchedEvent event) {                // 当执行 set /firstNode 2 时,触发这个 if 判断                // 注意: 这个监听器被绑定之后,只能使用一次                if (Event.EventType.NodeDataChanged.equals(event.getType())) {                    System.out.println("数据发生改变");                }            }        }, stat));        /**         * watch = true 使用默认的监听器,也就是创建 ZooKeeper 时的监听器         * watch = false 则表示监听器为空         */        client.getData("/firstNode", true, stat);        client.getData("/firstNode", true, new AsyncCallback.DataCallback() {            @Override            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {                System.out.println("回调机制");            }        }, null);        // 创建节点 --- 持久的        client.create("/data", "1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);        // 输入的时候结束程序        System.in.read();    }}