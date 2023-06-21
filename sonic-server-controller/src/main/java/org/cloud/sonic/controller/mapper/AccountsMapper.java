package org.cloud.sonic.controller.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.cloud.sonic.controller.models.domain.Accounts;
import org.cloud.sonic.controller.models.domain.Agents;
import org.cloud.sonic.controller.models.domain.Steps;

import java.util.List;

/**
 * Mapper 接口
 *
 * @author JayWenStar
 * @since 2021-12-17
 */
@Mapper
public interface AccountsMapper extends BaseMapper<Accounts> {

    @Select("SELECT * FROM accounts WHERE ud_id is NULL or ud_id = '' limit #{number}")
    List<Accounts> selectNotHaveUseAccounts(@Param("number") int number);


    @Select("UPDATE `accounts` SET `ud_id` = #{udId} WHERE `name` = #{name} ;")
    Integer updateUdId(@Param("name") String name, @Param("udId") String udId);


}
