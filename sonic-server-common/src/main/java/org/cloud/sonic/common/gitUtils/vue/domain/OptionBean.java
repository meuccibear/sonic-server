package org.cloud.sonic.common.gitUtils.vue.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: helium-pro-new
 * @ClassName OptionBean
 * @description:
 * @author: Mr.Lv
 * @email: lvzhuozhuang@foxmail.com
 * @create: 2022-03-20 08:12
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionBean {

    Object value;

    String label;

    List<OptionBean> children;

    public void pull(OptionBean optionBean) {
        if (null == children) {
            children = new ArrayList<>();
        }
        children.add(optionBean);
    }

    public OptionBean(Object value, String label) {
        this.value = value;
        this.label = label;
    }
}
