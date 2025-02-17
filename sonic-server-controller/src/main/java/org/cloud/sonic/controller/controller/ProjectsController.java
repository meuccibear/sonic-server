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
package org.cloud.sonic.controller.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.cloud.sonic.common.config.WebAspect;
import org.cloud.sonic.common.config.WhiteUrl;
import org.cloud.sonic.common.exception.SonicException;
import org.cloud.sonic.common.http.RespEnum;
import org.cloud.sonic.common.http.RespModel;
import org.cloud.sonic.controller.mapper.GlobalParamsMapper;
import org.cloud.sonic.controller.mapper.TestSuitesMapper;
import org.cloud.sonic.controller.models.base.TypeConverter;
import org.cloud.sonic.controller.models.domain.GlobalParams;
import org.cloud.sonic.controller.models.domain.Projects;
import org.cloud.sonic.controller.models.domain.TestSuites;
import org.cloud.sonic.controller.models.dto.ProjectsDTO;
import org.cloud.sonic.controller.services.GlobalParamsService;
import org.cloud.sonic.controller.services.ProjectsService;
import org.cloud.sonic.controller.services.TestSuitesService;
import org.cloud.sonic.controller.tools.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ZhouYiXun
 * @des
 * @date 2021/9/9 22:46
 */
@Tag(name = "项目管理相关")
@Slf4j
@RestController
@RequestMapping("/projects")
public class ProjectsController {

    @Autowired
    private ProjectsService projectsService;

    @Autowired
    TestSuitesMapper testSuitesService;

    @Autowired
    GlobalParamsMapper globalParamsService;

    @WebAspect
    @Operation(summary = "更新项目信息", description = "新增或更新项目信息")
    @PutMapping
    public RespModel<String> save(@Validated @RequestBody ProjectsDTO projects) {
        Projects projectsData = projects.convertTo();
        projectsService.save(projectsData);
        log.info("id:{} {}", projectsData.getId().toString(), projectsData.getId() == 1);
        if (projectsData.getId() == 1) {
            globalParamsService.insert(new GlobalParams(Constant.BASE_ID, "base", "{\"login\":0,\"liveenter\":0,\"livechat\":0,\"livelike\":0,\"liveleave\":0,\"livefollow\":0}", 1));
            testSuitesService.insert(new TestSuites(Constant.BASE_ID, 2, "执行脚本", 1, 0, 1000, 1, null));
        }
        return new RespModel<>(RespEnum.UPDATE_OK);
    }

    @WebAspect
    @WhiteUrl
    @Operation(summary = "查找所有项目", description = "查找所有项目列表")
    @GetMapping("/list")
    public RespModel<List<ProjectsDTO>> findAll() {
        return new RespModel<>(
                RespEnum.SEARCH_OK,
                projectsService.findAll().stream().map(TypeConverter::convertTo).collect(Collectors.toList())
        );
    }

    @WebAspect
    @Operation(summary = "查询项目信息", description = "查找对应id下的详细信息")
    @Parameter(name = "id", description = "项目id")
    @GetMapping
    public RespModel<?> findById(@RequestParam(name = "id") int id) {
        Projects projects = projectsService.findById(id);
        if (projects != null) {
            return new RespModel<>(RespEnum.SEARCH_OK, projects.convertTo());
        } else {
            return new RespModel<>(RespEnum.ID_NOT_FOUND);
        }
    }

    @WebAspect
    @Operation(summary = "删除", description = "删除对应id下的详细信息")
    @Parameter(name = "id", description = "项目id")
    @DeleteMapping
    public RespModel<String> delete(@RequestParam(name = "id") int id) throws SonicException {
        projectsService.delete(id);
        return new RespModel<>(RespEnum.DELETE_OK);
    }
}
