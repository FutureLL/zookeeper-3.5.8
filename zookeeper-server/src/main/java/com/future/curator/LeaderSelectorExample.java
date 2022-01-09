package com.future.curator;import org.apache.curator.framework.CuratorFramework;import org.apache.curator.framework.CuratorFrameworkFactory;import org.apache.curator.framework.recipes.leader.LeaderLatch;import org.apache.curator.framework.recipes.leader.LeaderSelector;import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;import org.apache.curator.framework.state.ConnectionState;import org.apache.curator.retry.RetryNTimes;import org.apache.curator.shaded.com.google.common.collect.Lists;import java.util.List;import java.util.concurrent.TimeUnit;/** * @Description: * @Author: lilei58 * @Date: Created in 2021/11/24 上午8:28 */public class LeaderSelectorExample {    public static void main(String[] args) throws Exception {        List<CuratorFramework> clientList = Lists.newArrayList();        List<LeaderSelector> selectorList = Lists.newArrayList();        for (int i = 0; i < 10; i++) {            CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", new RetryNTimes(3, 1000));            clientList.add(client);            client.start();            LeaderSelector selector = new LeaderSelector(client, "/LeaderSelector", new LeaderSelectorListener() {                @Override                public void takeLeadership(CuratorFramework client) throws Exception {                    // 当上Leader就会进入到这个方法                    System.out.println("当前 Leader 是: " + client);                    TimeUnit.SECONDS.sleep(5);                }                @Override                public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {                }            });            selectorList.add(selector);            selector.start();        }        System.in.read();        for (CuratorFramework client : clientList) {            client.close();        }        for (LeaderSelector selector : selectorList) {            selector.close();        }    }}