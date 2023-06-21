package org.cloud.sonic.controller.services;

import com.baomidou.mybatisplus.extension.service.IService;
import org.cloud.sonic.controller.models.domain.Accounts;
import java.util.List;

public interface AccountsService extends IService<Accounts> {

    List<Accounts> findAll(int projectId);

    boolean delete(int id);

    Accounts findById(int id);

    boolean deleteByProjectId(int projectId);

    List<Accounts> selectNotHaveUseAccounts(int number);

    Integer updateUdId(String name, String udId);

    Accounts findByName(String name);

    List<Accounts> findByNames(List<String> names);
}
