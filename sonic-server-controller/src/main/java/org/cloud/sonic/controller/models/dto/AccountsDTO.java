package org.cloud.sonic.controller.models.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import org.cloud.sonic.controller.models.base.TypeConverter;
import org.cloud.sonic.controller.models.domain.Accounts;
import org.cloud.sonic.controller.models.domain.Devices;

import java.io.Serializable;
import java.util.Set;

@Schema(name = "设备DTO 模型")
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountsDTO implements Serializable, TypeConverter<AccountsDTO, Accounts> {
// implements Serializable, TypeConverter<GlobalParamsDTO, GlobalParams>
    @Schema(description = "id", example = "1")
    Integer id;

    @Schema(description = "App编号", example = "0")
    String appName;

    @Schema(description = "用户名", example = "123111@qq.com")
    String name;

    @Schema(description = "密码", example = "123456")
    String password;

    @Schema(description = "序列号", example = "192.168.0.57:5555")
    String udId;

    @Schema(description = "帐号状态", example = "1")
    Integer status;

}
