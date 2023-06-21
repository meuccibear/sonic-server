package org.cloud.sonic.common.gitUtils;

import lombok.extern.slf4j.Slf4j;

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
public abstract class CounterUtil {

    Integer frequency = 3;

    public abstract boolean check(Object execute);

    public Object run(Object data) {
        log.info("run:frequency:{}",frequency);

        if (frequency > 0) {

            if (frequency == 1) {
                networkErr();
            }

            Object execute = null;
            try {
                execute = execute(data);
            } catch (Exception e) {
                log.error("run.execute", e);
            }

            if (check(execute)) {
                frequency--;
                return run(data);
            }

            return execute;
        } else {
            return null;
        }
    }

    public void networkErr() {
        try {
            Thread.sleep(1000 * 5);
            log.info("休息了 1000毫秒 * 5  ~~");
        } catch (InterruptedException e) {
            log.error("休息错误~~", e);
        }
    }

    public abstract Object execute(Object data) throws Exception;

    public CounterUtil(int frequency) {
        frequency = frequency;
    }

    public CounterUtil() {
    }

}
