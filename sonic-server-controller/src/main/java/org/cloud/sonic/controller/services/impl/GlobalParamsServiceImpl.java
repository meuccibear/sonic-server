/*
 *   sonic-server  ZPUTech Cloud Real Machine Platform.
 *   Copyright (C) 2022 ZPUTechCloudOrg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.cloud.sonic.controller.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.cloud.sonic.controller.mapper.GlobalParamsMapper;
import org.cloud.sonic.controller.models.domain.GlobalParams;
import org.cloud.sonic.controller.services.GlobalParamsService;
import org.cloud.sonic.controller.services.impl.base.SonicServiceImpl;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ZhouYiXun
 * @des
 * @date 2021/10/9 23:28
 */
@Order(1)
@Service("globalParamsService")
public class GlobalParamsServiceImpl extends SonicServiceImpl<GlobalParamsMapper, GlobalParams> implements GlobalParamsService {

    @Override
    public List<GlobalParams> findAll(int projectId, boolean additional) {

        LambdaQueryChainWrapper<GlobalParams> lambdaQuery = lambdaQuery().eq(GlobalParams::getProjectId, projectId);
        if (!additional) {
            lambdaQuery.ge(GlobalParams::getId, -1);
        }
        return lambdaQuery.list();

//        return lambdaQuery().eq(GlobalParams::getProjectId, projectId).ge(GlobalParams::getId, -1).list();
    }

    @Override
    public boolean delete(int id) {
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public GlobalParams findById(int id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean deleteByProjectId(int projectId) {
        return baseMapper.delete(new LambdaQueryWrapper<GlobalParams>().eq(GlobalParams::getProjectId, projectId)) > 0;
    }
}
