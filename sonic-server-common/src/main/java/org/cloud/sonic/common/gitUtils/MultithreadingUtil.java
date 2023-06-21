package org.cloud.sonic.common.gitUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

/**
 * @program: helium-pro-new
 * @ClassName CounterUtil
 * @description:
 * @author: Mr.Lv
 * @email: lvzhuozhuang@foxmail.com
 * @create: 2022-09-19 09:37
 * @Version 1.0
 **/
@Slf4j
public abstract class MultithreadingUtil<E> {

    Integer concurrency = 10;

    public void run(List<E> data) {
        if (ObjectUtils.notIsEmpty(data)) {
            List<List<E>> lists = ObjectUtils.averageAssignPartition(data, concurrency);
            for (int index = 0; index < lists.size(); index++) {
                try {
                    executes(lists.get(index), index);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public abstract void execute(E data);

    @Async("taskExecutor")
    public void executes(List<E> lists, int index) throws InstantiationException, IllegalAccessException {
        log.info(String.format("开始执行任务：hash值：%s 线程序号：%d任务量：%d", lists.hashCode(), concurrency, lists.size()));
        for (int i = 0; i < lists.size(); i++) {
            log.info(String.format("hash值：%s 线程序号：%d 任务量：%d 开始查询第%d个设备信息", lists.hashCode(), index, lists.size(), i));
//            execute(String.format("hash值：%s 线程序号：%d 任务量：%d 开始查询第%d个设备信息", datas.hashCode(), index, datas.size(), i), lists.get(i));
            execute(lists.get(i));
        }
    }

    public MultithreadingUtil(int concurrency) {
        concurrency = concurrency;
    }

    public MultithreadingUtil() {
    }

}
