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
import org.cloud.sonic.common.config.WebAspect;
import org.cloud.sonic.common.http.RespEnum;
import org.cloud.sonic.common.http.RespModel;
import org.cloud.sonic.controller.models.domain.Accounts;
import org.cloud.sonic.controller.models.dto.AccountsDTO;
import org.cloud.sonic.controller.services.AccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @author ZhouYiXun
 * @des
 * @date 2021/8/28 21:49
 */
@Tag(name = "Account管理")
@RestController
@RequestMapping("/accounts")
public class AccountsController {

    @Autowired
    private AccountsService accountsService;

    @WebAspect
    @Operation(summary = "更新帐号", description = "新增或更新对应的帐号")
    @PutMapping
    public RespModel<String> save(@Validated @RequestBody AccountsDTO AccountsDTO) {
        accountsService.save(AccountsDTO.convertTo());
        return new RespModel<>(RespEnum.UPDATE_OK);
    }

    @WebAspect
    @Operation(summary = "查找帐号", description = "查找对应项目id的帐号列表")
    @Parameter(name = "projectId", description = "项目id")
    @GetMapping("/list")
    public RespModel<List<Accounts>> findByProjectId(@RequestParam(name = "projectId") int projectId) {
        return new RespModel<>(RespEnum.SEARCH_OK, accountsService.findAll(projectId));
    }

    @WebAspect
    @Operation(summary = "删除帐号", description = "删除对应id的帐号")
    @Parameter(name = "id", description = "id")
    @DeleteMapping
    public RespModel<String> delete(@RequestParam(name = "id") int id) {
        if (accountsService.delete(id)) {
            return new RespModel<>(RespEnum.DELETE_OK);
        } else {
            return new RespModel<>(RespEnum.DELETE_FAIL);
        }
    }

    @WebAspect
    @Operation(summary = "查看帐号信息", description = "查看对应id的帐号")
    @Parameter(name = "id", description = "id")
    @GetMapping
    public RespModel<Accounts> findById(@RequestParam(name = "id") int id) {
        Accounts Accounts = accountsService.findById(id);
        if (Accounts != null) {
            return new RespModel<>(RespEnum.SEARCH_OK, Accounts);
        } else {
            return new RespModel<>(RespEnum.ID_NOT_FOUND);
        }
    }
}
