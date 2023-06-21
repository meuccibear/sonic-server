package org.cloud.sonic.controller.tools;

import com.alibaba.fastjson.JSONObject;
import org.cloud.sonic.common.tools.SpringContextUtils;
import org.cloud.sonic.controller.models.domain.GlobalParams;
import org.cloud.sonic.controller.models.interfaces.ActionType;
import org.cloud.sonic.controller.services.GlobalParamsService;


public final class Constant {

    public final static Integer BASE_ID = -10;


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
            }
        }
        return baseJson.getInteger(actionType.getValue());
    }


}
