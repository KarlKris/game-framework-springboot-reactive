package com.li.gamecommon.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * @author li-yuanwen
 */
@Slf4j
public class MonitoredScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {


    private ConcurrentHashMap<String, Long> startTimes;
    private int maxNum;
    private int maxTime;

    public MonitoredScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
        this.startTimes = new ConcurrentHashMap<>(0);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {

        if (log.isDebugEnabled()) {
            // 统计正在执行的任务数量、已完成任务数量、任务总数、队列里缓存的任务数量、池中存在的最大线程数

            int size = this.getQueue().size();
            log.debug("可监控定时线程池正在执行的任务数量[{}], 已完成任务数量[{}], 任务总数[{}], 队列里缓存的任务数量[{}], 池中存在的最大线程数[{}]"
                    , this.getActiveCount()
                    , this.getCompletedTaskCount()
                    , this.getTaskCount()
                    , size
                    , this.getLargestPoolSize());

            this.maxNum = Math.max(this.maxNum, size);

            this.startTimes.put(toRunnableId(r), System.currentTimeMillis());
        }

    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (log.isDebugEnabled()) {
            String id = toRunnableId(r);
            long now = System.currentTimeMillis();
            long startTime = this.startTimes.remove(id);

            int time = (int) (now - startTime);
            this.maxTime = Math.max(this.maxTime, time);

            log.debug("可监控定时线程池 任务[{}]耗时[{}]", id, time);
        }
    }

    @Override
    public void shutdown() {
        log();
        super.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        log();
        return super.shutdownNow();
    }

    private String toRunnableId(Runnable t) {
        return String.valueOf(t.hashCode());
    }

    private void log() {
        if (log.isDebugEnabled()) {
            log.debug("可监控定时线程池队列最大缓存数量[{}], 总完成任务数[{}]", this.maxNum, this.getCompletedTaskCount());
        }
    }
}
