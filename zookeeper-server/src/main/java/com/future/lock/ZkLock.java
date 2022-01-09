package com.future.lock;import org.apache.zookeeper.*;import java.util.Collections;import java.util.List;import java.util.Optional;import java.util.concurrent.CountDownLatch;import java.util.concurrent.TimeUnit;import java.util.concurrent.locks.Condition;import java.util.concurrent.locks.Lock;/** * @Description: * @Author: lilei58 * @Date: Created in 2022/1/5 上午7:45 */public class ZkLock implements Lock {    private ThreadLocal<ZooKeeper> zk = new ThreadLocal<>();    private static final String LOCK_NAME = "/LOCK";    private ThreadLocal<String> CURRENT_NODE_NAME = new ThreadLocal<>();    public void init() {        if (zk.get() == null) {            try {                zk.set(new ZooKeeper("localhost:2181", 300, watchedEvent -> {}));            } catch (Exception e) {                e.printStackTrace();            }        }    }    @Override    public void lock() {        init();        if (tryLock()) {            System.out.println(Thread.currentThread().getName() + " 已经获取到了锁");        }    }    @Override    public boolean tryLock() {        String nodeName = LOCK_NAME + "/zk_";        try {            // 创建节点            CURRENT_NODE_NAME.set(zk.get().create(nodeName, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL));            // 获取父节点下的所有子节点            List<String> childrenNodeList = zk.get().getChildren(LOCK_NAME, watchedEvent -> {});            // 排序            Collections.sort(childrenNodeList);            // 获取最小的节点            Optional<String> first = childrenNodeList.stream().findFirst();            String minNodeName = first.orElse("");            if (CURRENT_NODE_NAME.equals(LOCK_NAME + "/" + minNodeName)) {                return true;            } else {                // 值为1的并发锁                CountDownLatch countDownLatch = new CountDownLatch(1);                // 获取创建节点的下标                int currentNodeIndex = childrenNodeList.indexOf(CURRENT_NODE_NAME.get().substring(CURRENT_NODE_NAME.get().lastIndexOf('/') + 1));                // 子节点为空,说明可以获取锁                if (currentNodeIndex - 1 <= -1) {                    return true;                }                String prevNodeName = childrenNodeList.get(currentNodeIndex - 1);                // 阻塞,等待前一个节点的删除事件发生后才返回 true                zk.get().exists(LOCK_NAME + "/" + prevNodeName, watchedEvent -> {                    if (Watcher.Event.EventType.NodeDeleted.equals(watchedEvent.getType())) {                        // ...                        // 唤醒,countDownLatch 值减为0                        countDownLatch.countDown();                        System.out.println(Thread.currentThread().getName() + " 被唤醒...");                    }                });                System.out.println(Thread.currentThread().getName() + " 线程阻塞...");                // 等待                countDownLatch.await();                return true;            }        } catch (KeeperException e) {            e.printStackTrace();        } catch (InterruptedException e) {            e.printStackTrace();        }        return false;    }    @Override    public void unlock() {        try {            // -1: 忽略版本号,强制删除            zk.get().delete(CURRENT_NODE_NAME.get(), -1);            CURRENT_NODE_NAME.set(null);            zk.get().close();        } catch (Exception e) {            e.printStackTrace();        }    }    @Override    public void lockInterruptibly() throws InterruptedException {    }    @Override    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {        return false;    }    @Override    public Condition newCondition() {        return null;    }}