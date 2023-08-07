package org.cloud.sonic.controller.tools;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.cloud.sonic.common.tools.SpringContextUtils;
import org.cloud.sonic.controller.models.domain.GlobalParams;
import org.cloud.sonic.controller.models.interfaces.ActionType;
import org.cloud.sonic.controller.services.GlobalParamsService;


@Slf4j
public final class Constant {

    public final static Integer BASE_ID = 9999;


    private static GlobalParamsService globalParamsService;

    static {
        Constant.globalParamsService = (GlobalParamsService) SpringContextUtils.getBean("globalParamsService");

    }

    private static JSONObject baseJson = null;

    public static Integer getActionId(ActionType actionType) {
        if (null == baseJson) {
            GlobalParams globalParams = globalParamsService.findById(Constant.BASE_ID);
            if (null != globalParams && null != globalParams.getParamsValue()) {
                baseJson = JSONObject.parseObject(globalParams.getParamsValue());
            }else {
                log.error("没有配置文件～～");
                return null;
            }
        }
        return baseJson.getInteger(actionType.getValue());
    }


}
