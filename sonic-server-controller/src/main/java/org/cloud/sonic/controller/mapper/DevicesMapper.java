package org.cloud.sonic.controller.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.cloud.sonic.controller.models.domain.Devices;
import org.cloud.sonic.controller.models.dto.StepsDTO;

import java.util.Collection;
import java.util.List;

/**
 * Mapper 接口
 *
 * @author JayWenStar
 * @since 2021-12-17
 */
@Mapper
public interface DevicesMapper extends BaseMapper<Devices> {

    @Select("select cpu from devices group by cpu")
    List<String> findCpuList();

    @Select("select size from devices group by size")
    List<String> findSizeList();

    @Select("select d.* from test_suites_devices tsd " +
            "inner join devices d on d.id = tsd.devices_id " +
            "where tsd.test_suites_id = #{TestSuitesId} " +
            "order by tsd.sort asc")
    List<Devices> listByTestSuitesId(@Param("TestSuitesId") int TestSuitesId);

    @Select("<script>" +
            "select d.* from test_suites_devices tsd " +
            "inner join devices d on d.id = tsd.devices_id " +
            "where tsd.test_suites_id in \n" +
            "<foreach collection='TestSuitesIds' item='item' index='index' open='(' close=')' separator=','>#{item}</foreach>\n" +
            "order by tsd.sort asc" +
            "</script>")
    List<Devices> listByTestSuitesIds(@Param("TestSuitesIds") List<Integer> TestSuitesIds);

    @Select("<script> SELECT * FROM devices where id in <foreach item='item' index='index' collection='params' open='(' separator=',' close=')'>#{item}</foreach> </script>")
    List<Devices> getDevices(@Param("params") List<Integer> params);

    @Select("<script> SELECT * FROM devices where ud_id in <foreach item='item' index='index' collection='params' open='(' separator=',' close=')'>#{item}</foreach> </script>")
    List<Devices> getDevicesByUdId(@Param("params") List<String> params);
}
