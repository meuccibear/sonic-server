package org.cloud.sonic.controller.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.cloud.sonic.controller.mapper.AccountsMapper;
import org.cloud.sonic.controller.models.domain.Accounts;
import org.cloud.sonic.controller.services.AccountsService;
import org.cloud.sonic.controller.services.impl.base.SonicServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service("accountsService")
@Slf4j
public class AccountsServiceImpl extends SonicServiceImpl<AccountsMapper, Accounts> implements AccountsService {

    @Resource
    AccountsMapper accountsMapper;

    @Override
    public List<Accounts> findAll(int projectId) {
        return lambdaQuery().eq(Accounts::getProjectId, projectId).list();
    }

    @Override
    public boolean delete(int id) {
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public Accounts findById(int id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean deleteByProjectId(int projectId) {
        return baseMapper.delete(new LambdaQueryWrapper<Accounts>().eq(Accounts::getProjectId, projectId)) > 0;
    }

    @Override
    public List<Accounts> selectNotHaveUseAccounts(int number) {
        return accountsMapper.selectNotHaveUseAccounts(number);
    }

    @Override
    public Integer updateUdId(String name, String udId) {
        return accountsMapper.updateUdId(name, udId);
    }

    @Override
    public Accounts findByName(String name) {
        List<Accounts> accountsList = lambdaQuery().eq(Accounts::getName, name).list();
        if (null != accountsList && accountsList.size() > 0) {
            return accountsList.get(0);
        }
        return null;
    }

    public List<Accounts> findByNames(List<String> names) {
        LambdaQueryChainWrapper<Accounts> chainWrapper = new LambdaQueryChainWrapper<>(accountsMapper);
        chainWrapper.and(i -> {
            if (names != null) {
                i.or().and(j -> {
                    for (String v : names) {
                        j.or().likeRight(Accounts::getName, v);
                    }
                });
            }
        });

        List<Accounts> accountsList = chainWrapper.list();
        Map<String, Accounts> map = new HashMap<>();


        return accountsList;
    }
}
