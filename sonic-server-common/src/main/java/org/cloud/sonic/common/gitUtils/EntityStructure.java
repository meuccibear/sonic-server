package org.cloud.sonic.common.gitUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: helium-pro-new
 * @ClassName EntityStructure
 * @description:
 * @author: Mr.Lv
 * @email: lvzhuozhuang@foxmail.com
 * @create: 2022-04-02 00:44
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityStructure {
    String name;
    String type;
    List<EntityStructure> entityStructureList;

    public void add(String name, String type){
        if(ObjectUtils.isEmpty(entityStructureList)){
            entityStructureList = new ArrayList<>();
        }
        entityStructureList.add(new EntityStructure(name, type, null));
    }
}
