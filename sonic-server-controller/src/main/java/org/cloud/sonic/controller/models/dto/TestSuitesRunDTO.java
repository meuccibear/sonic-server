package org.cloud.sonic.controller.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Schema(name = "测试套件DTO 模型")
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSuitesRunDTO implements Serializable {

    @Positive
    @Schema(description = "用例编号", required = true, example = "1")
    Integer caseId;

    @NotNull
    @Schema(description = "分组", required = true, example = "1")
    Integer groupId;

    //因为一个控件可以存在于多个步骤，也可以一个步骤有多个同样的控件，所以是多对多关系
    List<StepsDTO> steps;

}

