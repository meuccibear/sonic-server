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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.cloud.sonic.common.gitUtils.JSONUtils;
import org.cloud.sonic.common.gitUtils.StringUtils;
import org.cloud.sonic.common.http.RespEnum;
import org.cloud.sonic.common.http.RespModel;
import org.cloud.sonic.controller.mapper.*;
import org.cloud.sonic.controller.models.base.CommentPage;
import org.cloud.sonic.controller.models.base.TypeConverter;
import org.cloud.sonic.controller.models.domain.*;
import org.cloud.sonic.controller.models.dto.*;
import org.cloud.sonic.controller.models.enums.ConditionEnum;
import org.cloud.sonic.controller.models.interfaces.*;
import org.cloud.sonic.controller.services.*;
import org.cloud.sonic.controller.services.impl.base.SonicServiceImpl;
import org.cloud.sonic.controller.tools.Constant;
import org.cloud.sonic.controller.tools.ElTreeBuilder;
import org.cloud.sonic.controller.transport.TransportWorker;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ZhouYiXun
 * @des 测试套件逻辑实现
 * @date 2021/8/20 17:51
 */
@Service
@Slf4j
public class TestSuitesServiceImpl extends SonicServiceImpl<TestSuitesMapper, TestSuites> implements TestSuitesService, ApplicationContextAware {

    @Autowired
    private TestCasesMapper testCasesMapper;
    @Autowired
    private DevicesMapper devicesMapper;
    @Autowired
    private ResultsService resultsService;
    @Autowired
    private GlobalParamsService globalParamsService;
    @Autowired
    private StepsService stepsService;
    @Autowired
    private PublicStepsService publicStepsService;
    @Autowired
    private TestSuitesTestCasesMapper testSuitesTestCasesMapper;
    @Autowired
    private TestSuitesDevicesMapper testSuitesDevicesMapper;
    @Autowired
    private AgentsService agentsService;
    @Autowired
    private PackagesService packagesService;


    private Map<Integer, CoverHandler> coverHandlerMap;

    private ApplicationContext applicationContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RespModel<Integer> runSuite(int suiteId, String strike) {
        TestSuitesDTO testSuitesDTO = findById(suiteId);
        if (testSuitesDTO == null) {
            return new RespModel<>(3001, "suite.deleted");
        }

        if (testSuitesDTO.getTestCases().size() == 0) {
            return new RespModel<>(3002, "suite.empty.cases");
        }

        List<Devices> devicesList = testSuitesDTO.getDevices().stream().map(DevicesDTO::convertTo).collect(Collectors.toList());
        for (int i = devicesList.size() - 1; i >= 0; i--) {
            if (devicesList.get(i).getStatus().equals(DeviceStatus.OFFLINE) || devicesList.get(i).getStatus().equals(DeviceStatus.DISCONNECTED)) {
                devicesList.remove(devicesList.get(i));
            }
        }
        if (devicesList.size() == 0) {
            return new RespModel<>(3003, "suite.not.free.device");
        }

        // 初始化部分结果状态信息
        Results results = new Results();
        results.setStatus(ResultStatus.RUNNING);
        results.setSuiteId(suiteId);
        results.setSuiteName(testSuitesDTO.getName());
        results.setStrike(strike);
        if (testSuitesDTO.getCover() == CoverType.CASE) {
            results.setSendMsgCount(testSuitesDTO.getTestCases().size());
        }
        if (testSuitesDTO.getCover() == CoverType.DEVICE) {
            results.setSendMsgCount(testSuitesDTO.getTestCases().size() * devicesList.size());
        }
        results.setReceiveMsgCount(0);
        results.setProjectId(testSuitesDTO.getProjectId());
        resultsService.save(results);

        //组装全局参数为json对象
        List<GlobalParams> globalParamsList = globalParamsService.findAll(testSuitesDTO.getProjectId(), false);

        //将包含|的拆开多个参数并打乱，去掉json对象多参数的字段
        Map<String, List<String>> valueMap = new HashMap<>();
        JSONObject gp = new JSONObject();
        for (GlobalParams g : globalParamsList) {
            if (g.getParamsValue().contains("|")) {
                List<String> shuffle = new ArrayList<>(Arrays.asList(g.getParamsValue().split("\\|")));
                Collections.shuffle(shuffle);
                valueMap.put(g.getParamsKey(), shuffle);
            } else {
                gp.put(g.getParamsKey(), g.getParamsValue());
            }
        }
        coverHandlerMap.get(testSuitesDTO.getCover()).handlerSuite(testSuitesDTO, gp, devicesList, valueMap, results);
        return new RespModel<>(RespEnum.HANDLE_OK, results.getId());
    }

    @Autowired
    AccountsService accountsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RespModel<Integer> run(TestSuitesRunDTO testSuitesRunDTO, String strike) {

        if (null != testSuitesRunDTO.getSteps()) {
            List<StepsDTO> steps = new ArrayList<>();
            for (StepsDTO step : testSuitesRunDTO.getSteps()) {
                if (step.getDisabled().equals(0)) {
                    steps.add(step);
                }
            }
            testSuitesRunDTO.setSteps(steps);
        }
        String association = "Association";
        if (-10 == testSuitesRunDTO.getCaseId()) {
            // 脚本
//            {"liveenter":1,"livechat":2,"livelike":3,"liveleave":4,"livefollow":5}
            if (null != testSuitesRunDTO.getScript()) {
                try {
                    JSONObject jsonObject = JSON.parseObject(testSuitesRunDTO.getScript());
                    if (jsonObject.containsKey("Order")) {
                        return runScriptBook(jsonObject.getJSONArray("Order"), strike);
                    } else if (jsonObject.containsKey(association)) {
                        List<Accounts> accountsList = new ArrayList<>();
                        JSONArray accountArr = jsonObject.getJSONArray(association);
                        JSONObject account = null;
                        Accounts accounts;
                        for (int i = 0; i < accountArr.size(); i++) {
                            account = accountArr.getJSONObject(i);
                            accounts = JSON.toJavaObject(account, Accounts.class);
                            accountsList.add(accounts);
                        }
                        addAccountv2(accountsList, testSuitesRunDTO.getId());
                        return new RespModel<>(RespEnum.HANDLE_OK);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return new RespModel<>(3001, "suite.script.err");
        }

        TestSuitesDTO testSuitesDTO = findById(testSuitesRunDTO);
        if (testSuitesDTO == null) {
            return new RespModel<>(3001, "suite.deleted");
        }

        if (testSuitesDTO.getTestCases().size() == 0) {
            return new RespModel<>(3002, "suite.empty.cases");
        }

        List<Devices> devicesList = testSuitesDTO.getDevices().stream().map(DevicesDTO::convertTo).collect(Collectors.toList());
        for (int i = devicesList.size() - 1; i >= 0; i--) {
            if (devicesList.get(i).getStatus().equals(DeviceStatus.OFFLINE) || devicesList.get(i).getStatus().equals(DeviceStatus.DISCONNECTED)) {
                devicesList.remove(devicesList.get(i));
            }
        }
        if (devicesList.size() == 0) {
            return new RespModel<>(3003, "suite.not.free.device");
        }

        // 初始化部分结果状态信息
        Results results = new Results();
        results.setStatus(ResultStatus.RUNNING);
        results.setSuiteId(testSuitesRunDTO.getGroupId());
        results.setSuiteName(testSuitesDTO.getName());
        results.setStrike(strike);
        if (testSuitesDTO.getCover() == CoverType.CASE) {
            results.setSendMsgCount(testSuitesDTO.getTestCases().size());
        }
        if (testSuitesDTO.getCover() == CoverType.DEVICE) {
            results.setSendMsgCount(testSuitesDTO.getTestCases().size() * devicesList.size());
        }
        results.setReceiveMsgCount(0);
        results.setProjectId(testSuitesDTO.getProjectId());
        resultsService.save(results);

        //组装全局参数为json对象
        List<GlobalParams> globalParamsList = globalParamsService.findAll(testSuitesDTO.getProjectId(), false);

        //将包含|的拆开多个参数并打乱，去掉json对象多参数的字段
        Map<String, List<String>> valueMap = new HashMap<>();
        JSONObject gp = new JSONObject();
        for (GlobalParams g : globalParamsList) {
            if (g.getParamsValue().contains("|")) {
                List<String> shuffle = new ArrayList<>(Arrays.asList(g.getParamsValue().split("\\|")));
                Collections.shuffle(shuffle);
                valueMap.put(g.getParamsKey(), shuffle);
            } else {
                gp.put(g.getParamsKey(), g.getParamsValue());
            }
        }
        log.info("testSuitesDTO:{}", JSON.toJSONString(testSuitesDTO));

        // TODO 帐号
        List<Accounts> accountsList = null;
        // 查找是否有登陆用例 若有 则获取和设备相同数量的帐号
        if (StringUtils.notIsEmpty(testSuitesRunDTO.getScript())) {
            accountsList = new ArrayList<>();
            JSONObject accountData = (JSONObject) JSONUtils.toJSON(testSuitesRunDTO.getScript());
            if (ObjectUtils.isEmpty(accountData)) {
                String[] rows = testSuitesRunDTO.getScript().split("\n");
                String[] col;
                Accounts accounts;
                for (String row : rows) {
                    col = row.split(",");
                    if (col.length == 2) {
                        accounts = new Accounts(null, "Tiktok", col[0], col[1], testSuitesDTO.getProjectId(), col[2], col[3],
                                null, null);
                        accountsList.add(accounts);
                    }
                }
            } else {
                JSONArray accountArr = accountData.getJSONArray("Accounts");
                JSONObject accountItem = null;
                Accounts accounts;
                for (int i = 0; i < accountArr.size(); i++) {
                    accountItem = accountArr.getJSONObject(i);
                    // proxy session
                    accounts = new Accounts(null, "Tiktok", accountItem.getString("name"), accountItem.getString("password"), testSuitesDTO.getProjectId(), accountItem.getString("proxy"), accountItem.getString("session"), null, null);
                    accountsList.add(accounts);
                }
            }
            accountsList = addAccount(accountsList);

        } else {
            for (TestCasesDTO testCase : testSuitesDTO.getTestCases()) {
                if (Constant.getActionId(ActionType.LOGIN).equals(testCase.getId())) {
                    accountsList = accountsService.selectNotHaveUseAccounts(devicesList.size());
                    break;
                }
            }
        }

        log.info("{} {} ", JSONUtils.toJSON(devicesList), JSONUtils.toJSON(accountsList));
        if (null != accountsList && devicesList.size() != accountsList.size()) {
            return new RespModel(3004, "suite.account.insufficient");
        }
        testSuitesDTO.pullSupper("accounts", accountsList);

        coverHandlerMap.get(testSuitesDTO.getCover()).handlerSuite(testSuitesDTO, gp, devicesList, valueMap, results);
        return new RespModel<>(RespEnum.HANDLE_OK, results.getId());
    }

    private List<Accounts> addAccount(List<Accounts> accountsList) {
        List<Accounts> result = new ArrayList<>();
        Accounts dbAccounts;
        for (Accounts accounts : accountsList) {
            dbAccounts = accountsService.findByName(accounts.getName());
            if (null == dbAccounts) {
                accountsService.save(accounts);
                result.add(accounts);
            } else if (null == dbAccounts.getUdId() || dbAccounts.getUdId().equals("")) {
                result.add(accounts);
//                return new RespModel(3004, "suite.account.insufficient");
            }
        }
        return result;
    }

    private void addAccountv2(List<Accounts> accountsList, Integer projectId) {
        Accounts dbAccounts;
        for (Accounts accounts : accountsList) {
            dbAccounts = accountsService.findByName(accounts.getName());
            accounts.setStatus(org.cloud.sonic.common.gitUtils.ObjectUtils.isEmpty(accounts.getUdId()) ? 1 : 2);
            if (null == dbAccounts) {
                accounts.setAppName("Tiktok");
                accounts.setProjectId(projectId);
                if(ObjectUtils.isEmpty(accounts.getPassword())){
                    accounts.setPassword("");
                }
                accountsService.save(accounts);
            } else {
                accounts.setId(dbAccounts.getId());
                accountsService.updateById(accounts);
            }
        }
    }

    private RespModel<Integer> runScriptBook(JSONArray orderArr, String strike) {
        JSONObject order;
        List<String> names = new ArrayList<>();
        orderArr.forEach(item -> {
            JSONObject jsonObject = (JSONObject) item;
            names.add(jsonObject.getString("name"));
        });

        List<Accounts> accountsList = accountsService.findByNames(names);
        Map<String, Accounts> accountMap = new HashMap<>();
        List<String> udIdList = new ArrayList<>();
        for (Accounts accounts : accountsList) {
            accountMap.put(accounts.getName(), accounts);
            udIdList.add(accounts.getUdId());
            if (StringUtils.isEmpty(accounts.getUdId())) {
                return new RespModel<>(3003, "suite.account.device");
            }
        }

        log.info(JSON.toJSONString(udIdList));
        if(StringUtils.isEmpty(udIdList)){
            return new RespModel<>(3003, "suite.account.null");
        }

        List<Devices> devicesList = devicesMapper.getDevicesByUdId(udIdList);
        // 检查在线
        for (int i = devicesList.size() - 1; i >= 0; i--) {
            if (devicesList.get(i).getStatus().equals(DeviceStatus.OFFLINE) || devicesList.get(i).getStatus().equals(DeviceStatus.DISCONNECTED)) {
                devicesList.remove(devicesList.get(i));
            }
        }
        if (devicesList.size() == 0) {
            return new RespModel<>(3003, "suite.not.free.device");
        }
        Map<String, Devices> devicesMap = new HashMap<>();
        for (Devices devices : devicesList) {
            devicesMap.put(devices.getUdId(), devices);
        }

        TestSuitesDTO testSuitesDTO  = baseMapper.selectById(Constant.BASE_ID).convertTo();
        if (testSuitesDTO == null) {
            return new RespModel<>(3001, "suite.deleted");
        }

        Integer delayed;
        for (int orderId = 0; orderId < orderArr.size(); orderId++) {
            order = orderArr.getJSONObject(orderId);
            delayed = order.getInteger("delayed");

            runScript(order, strike, accountsList, accountMap, devicesMap, testSuitesDTO);
            if (null != delayed && 0 != delayed) {
                try {
                    Thread.sleep(delayed * 1000);
                } catch (InterruptedException e) {
                }
            }

        }
        return new RespModel<>(RespEnum.HANDLE_OK);
    }

    private RespModel<Integer> runScript(JSONObject jsonObject, String strike, List<Accounts> accountsList,
                                         Map<String, Accounts> accountMap,
                                         Map<String, Devices> devicesMap, TestSuitesDTO testSuitesDTO) {
        String name = jsonObject.getString("name");
        String action = jsonObject.getString("action");

        ActionType actionType = ActionType.valueOf(action.toUpperCase());
        Integer suid = Constant.getActionId(actionType);

        List<Devices> devices = new ArrayList<>();
        log.info("suid: {}", suid);
        devices.add(devicesMap.get(accountMap.get(name).getUdId()));

        List<TestCasesDTO> testCasesDTOList = testCasesMapper.listById(suid)
                .stream().map(TypeConverter::convertTo).collect(Collectors.toList());
        for (TestCasesDTO testCasesDTO : testCasesDTOList) {
            testCasesDTO.setSteps(stepsService.findByCaseIdOrderBySort(testCasesDTO.getId(), true));
        }
        testSuitesDTO.setTestCases(testCasesDTOList);

        // 初始化部分结果状态信息
        Results results = new Results();
        results.setStatus(ResultStatus.RUNNING);
        results.setSuiteId(testSuitesDTO.getId());
        results.setSuiteName(testSuitesDTO.getName());
        results.setStrike(strike);
        if (testSuitesDTO.getCover() == CoverType.CASE) {
            results.setSendMsgCount(testSuitesDTO.getTestCases().size());
        }
        if (testSuitesDTO.getCover() == CoverType.DEVICE) {
            results.setSendMsgCount(testSuitesDTO.getTestCases().size() * devices.size());
        }
        results.setReceiveMsgCount(0);
        results.setProjectId(testSuitesDTO.getProjectId());
        resultsService.save(results);

        // 填充devices
        testSuitesDTO.setDevices(devices.stream().map(TypeConverter::convertTo).collect(Collectors.toList()));

        //将包含|的拆开多个参数并打乱，去掉json对象多参数的字段
        Map<String, List<String>> valueMap = new HashMap<>();
        JSONObject gp = new JSONObject();
        testSuitesDTO.getSupper().putAll(jsonObject);

        log.info("testSuitesDTO: {}", JSON.toJSONString(testSuitesDTO));

        coverHandlerMap.get(testSuitesDTO.getCover()).handlerSuite(testSuitesDTO, gp, devices, valueMap, results);

        return null;
    }

    @Override
    public List<ElTreeBuilder.NodeBuild> elSelect(int projectId) {
        List<TestSuites> testSuitesList = lambdaQuery().eq(TestSuites::getProjectId, projectId)
                .orderByDesc(TestSuites::getId)
                .list();

        List<ElTreeBuilder.NodeBuild> nodes = new ArrayList<>();

        List<DevicesDTO> devicesDTOList = null;
        for (TestSuites testSuites : testSuitesList) {
            devicesDTOList = devicesMapper.listByTestSuitesId(testSuites.getId())
                    .stream().map(TypeConverter::convertTo).collect(Collectors.toList());

            List<ElTreeBuilder.NodeBuild> children = new ArrayList<>();

            for (DevicesDTO devicesDTO : devicesDTOList) {
                children.add(new ElTreeBuilder.NodeBuild("device_" + devicesDTO.getId(), devicesDTO.getUdId(), null));
            }
            nodes.add(new ElTreeBuilder.NodeBuild("group_" + testSuites.getId(), testSuites.getName(), children));

        }

        return nodes;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public RespModel<String> forceStopSuite(int resultId, String strike) {

        Results results = resultsService.findById(resultId);
        // 统计不在线的agent
        List<Integer> offLineAgentIds = new ArrayList<>();
        if (ObjectUtils.isEmpty(results)) {
            return new RespModel<>(3001, "suite.empty.result");
        }
        int suiteId = results.getSuiteId();

        TestSuitesDTO testSuitesDTO;
        if (existsById(suiteId)) {
            testSuitesDTO = findById(suiteId);
        } else {
            return new RespModel<>(3001, "suite.deleted");
        }

        if (testSuitesDTO.getTestCases().size() == 0) {
            return new RespModel<>(3002, "suite.empty.cases");
        }

        List<Devices> devicesList = testSuitesDTO.getDevices().stream().map(DevicesDTO::convertTo).collect(Collectors.toList());
        for (int i = devicesList.size() - 1; i >= 0; i--) {
            if (devicesList.get(i).getStatus().equals(DeviceStatus.OFFLINE) || devicesList.get(i).getStatus().equals(DeviceStatus.DISCONNECTED)) {
                devicesList.remove(devicesList.get(i));
            }
        }
        if (devicesList.size() == 0) {
            return new RespModel<>(3003, "suite.can.not.connect.device");
        }

        results.setStatus(ResultStatus.FAIL);
        results.setStrike(strike);
        if (testSuitesDTO.getCover() == CoverType.CASE) {
            results.setSendMsgCount(testSuitesDTO.getTestCases().size());
        }
        if (testSuitesDTO.getCover() == CoverType.DEVICE) {
            results.setSendMsgCount(testSuitesDTO.getTestCases().size() * devicesList.size());
        }
        results.setProjectId(testSuitesDTO.getProjectId());
        resultsService.save(results);

        int deviceIndex = 0;
        if (testSuitesDTO.getCover() == CoverType.CASE) {
            List<JSONObject> suiteDetail = new ArrayList<>();
            Set<Integer> agentIds = new HashSet<>();
            for (TestCasesDTO testCases : testSuitesDTO.getTestCases()) {
                JSONObject suite = new JSONObject();
                suite.put("cid", testCases.getId());
                Devices devices = devicesList.get(deviceIndex);
                // 不要用List.of，它的实现ImmutableCollections无法被序列化
                suite.put("device", new ArrayList<>() {{
                    add(devices);
                }});
                if (deviceIndex == devicesList.size() - 1) {
                    deviceIndex = 0;
                } else {
                    deviceIndex++;
                }
                suite.put("rid", results.getId());
                agentIds.add(devices.getAgentId());
                suiteDetail.add(suite);
            }
            JSONObject result = new JSONObject();
            result.put("msg", "forceStopSuite");
            result.put("pf", testSuitesDTO.getPlatform());
            result.put("cases", suiteDetail);
            for (Integer id : agentIds) {
                TransportWorker.send(id, result);
            }
        }
        if (testSuitesDTO.getCover() == CoverType.DEVICE) {
            List<JSONObject> suiteDetail = new ArrayList<>();
            Set<Integer> agentIds = new HashSet<>();
            for (TestCasesDTO testCases : testSuitesDTO.getTestCases()) {
                JSONObject suite = new JSONObject();
                for (Devices devices : devicesList) {
                    agentIds.add(devices.getAgentId());
                }
                suite.put("cid", testCases.getId());
                suite.put("device", devicesList);
                suite.put("rid", results.getId());
                suiteDetail.add(suite);
            }
            JSONObject result = new JSONObject();
            result.put("msg", "forceStopSuite");
            result.put("pf", testSuitesDTO.getPlatform());
            result.put("cases", suiteDetail);
            for (Integer id : agentIds) {
                TransportWorker.send(id, result);
            }
        }
        return new RespModel<>(RespEnum.HANDLE_OK);
    }


    @Override
    @Transactional
    public TestSuitesDTO findById(int id) {
        if (existsById(id)) {
            TestSuitesDTO testSuitesDTO = baseMapper.selectById(id).convertTo();
            int suiteId = testSuitesDTO.getId();

            // 填充testcase
            List<TestCasesDTO> testCasesDTOList = testCasesMapper.listByTestSuitesId(suiteId)
                    .stream().map(TypeConverter::convertTo).collect(Collectors.toList());
            testSuitesDTO.setTestCases(testCasesDTOList);

            // 填充devices
            List<DevicesDTO> devicesDTOList = devicesMapper.listByTestSuitesId(suiteId)
                    .stream().map(TypeConverter::convertTo).collect(Collectors.toList());
            testSuitesDTO.setDevices(devicesDTOList);

            return testSuitesDTO;
        } else {
            return null;
        }
    }

    public TestSuitesDTO findById(int suiteId, TestSuitesRunDTO testSuitesRunDTO) {
        if (existsById(suiteId)) {
            TestSuitesDTO testSuitesDTO = baseMapper.selectById(suiteId).convertTo();

            List<TestCasesDTO> testCasesDTOList = testCasesMapper.listById(testSuitesRunDTO.getCaseId())
                    .stream().map(TypeConverter::convertTo).collect(Collectors.toList());
            for (TestCasesDTO testCasesDTO : testCasesDTOList) {
                if (testCasesDTO.getId().equals(testSuitesRunDTO.getCaseId())) {
                    testCasesDTO.setSteps(testSuitesRunDTO.getSteps());
                }
            }
            testSuitesDTO.setTestCases(testCasesDTOList);

            // 填充devices
            List<DevicesDTO> devicesDTOList = devicesMapper.listByTestSuitesId(testSuitesDTO.getId())
                    .stream().map(TypeConverter::convertTo).collect(Collectors.toList());
            testSuitesDTO.setDevices(devicesDTOList);

            return testSuitesDTO;
        } else {
            return null;
        }
    }

    public TestSuitesDTO findById(TestSuitesRunDTO testSuitesRunDTO) {
//        Integer suiteId = 1;
//        if (existsById(suiteId)) {
//            TestSuitesDTO testSuitesDTO = baseMapper.selectById(suiteId).convertTo();
//            List<TestCasesDTO> testCasesDTOList = testCasesMapper.listById(testSuitesRunDTO.getCaseId())
//                    .stream().map(TypeConverter::convertTo).collect(Collectors.toList());
//            for (TestCasesDTO testCasesDTO : testCasesDTOList) {
//                if (testCasesDTO.getId().equals(testSuitesRunDTO.getCaseId())) {
//                    testCasesDTO.setSteps(testSuitesRunDTO.getSteps());
//                }
//            }
//            testSuitesDTO.setTestCases(testCasesDTOList);
//
//            List<DevicesDTO> devicesDTOList = devicesMapper.getDevices(testSuitesRunDTO.getDeviceIds())
//                    .stream().map(TypeConverter::convertTo).collect(Collectors.toList());
//
//            testSuitesDTO.setDevices(devicesDTOList);
//
//            return testSuitesDTO;
//        }
//        return null;

        if (existsById(testSuitesRunDTO.getGroupId())) {
            TestSuitesDTO testSuitesDTO = baseMapper.selectById(testSuitesRunDTO.getGroupId()).convertTo();

            List<TestCasesDTO> testCasesDTOList = testCasesMapper.listById(testSuitesRunDTO.getCaseId())
                    .stream().map(TypeConverter::convertTo).collect(Collectors.toList());
            for (TestCasesDTO testCasesDTO : testCasesDTOList) {
                if (testCasesDTO.getId().equals(testSuitesRunDTO.getCaseId())) {
                    testCasesDTO.setSteps(testSuitesRunDTO.getSteps());
                }
            }
            testSuitesDTO.setTestCases(testCasesDTOList);

            // 填充devices
            List<DevicesDTO> devicesDTOList = devicesMapper.listByTestSuitesId(testSuitesDTO.getId())
                    .stream().map(TypeConverter::convertTo).collect(Collectors.toList());
            testSuitesDTO.setDevices(devicesDTOList);

            return testSuitesDTO;
        } else {
            return null;
        }
    }

    /**
     * @param steps
     * @return com.alibaba.fastjson.JSONObject
     * @author ZhouYiXun
     * @des 递归获取步骤
     * @date 2021/8/20 17:50
     */
    @Transactional
    @Override
    public JSONObject getStep(StepsDTO steps) {
        JSONObject step = new JSONObject();
        if (steps.getStepType().equals("install") && steps.getContent().equals("2")) {
            String plat = "unknown";
            if (steps.getPlatform() == PlatformType.ANDROID) {
                plat = "Android";
            }
            if (steps.getPlatform() == PlatformType.IOS) {
                plat = "iOS";
            }
            steps.setText(packagesService.findOne(steps.getProjectId(), steps.getText(), plat));
        }

        if (steps.getStepType().equals("publicStep")) {
            PublicStepsDTO publicStepsDTO = publicStepsService.findById(Integer.parseInt(steps.getText()), true);
            if (publicStepsDTO != null) {
                JSONArray publicStepsJson = new JSONArray();
                for (StepsDTO pubStep : publicStepsDTO.getSteps()) {
                    if (pubStep.getDisabled() == 1) {
                        continue;
                    }
                    publicStepsJson.add(getStep(pubStep));
                }
                step = (JSONObject) JSONObject.toJSON(steps);
                step.put("pubSteps", publicStepsJson);

                return step;
            }
        }

        // 如果是条件步骤则遍历子步骤
        if (!ConditionEnum.NONE.getValue().equals(steps.getConditionType())) {
            JSONObject stepsJsonObj = handleSteps(steps);
            step.put("step", stepsJsonObj);

            return step;
        }

        step.put("step", steps);
        return step;
    }

    // 获取步骤结构树
    public JSONObject handleSteps(StepsDTO steps) {
        JSONObject stepsJsonObj = (JSONObject) JSONObject.toJSON(steps);
        if (steps == null) {
            return stepsJsonObj;
        }
        if (steps.getDisabled() == 1) {
            return stepsJsonObj;
        }
        if (steps.getStepType().equals("publicStep")) {
            PublicStepsDTO publicStepsDTO = publicStepsService.findById(Integer.parseInt(steps.getText()), true);
            if (publicStepsDTO != null) {
                JSONArray publicStepsJson = new JSONArray();
                for (StepsDTO pubStep : publicStepsDTO.getSteps()) {
                    if (pubStep.getDisabled() == 1) {
                        continue;
                    }
                    publicStepsJson.add(getStep(pubStep));
                }
                stepsJsonObj.put("pubSteps", publicStepsJson);
//                stepsJsonObj.put("step", stepsService.handleStep(steps, true));
            }
        } else if (steps.getStepType().equals("install") && steps.getContent().equals("2")) {
            String plat = "unknown";
            if (steps.getPlatform() == PlatformType.ANDROID) {
                plat = "Android";
            }
            if (steps.getPlatform() == PlatformType.IOS) {
                plat = "iOS";
            }
            stepsJsonObj.put("text", packagesService.findOne(steps.getProjectId(), steps.getText(), plat));
        }

        if (CollectionUtils.isEmpty(steps.getChildSteps())) {
            return stepsJsonObj;
        }

        JSONArray childStepJsonObjs = new JSONArray();
        List<StepsDTO> childSteps = steps.getChildSteps();

        for (StepsDTO childStep : childSteps) {
            if (childStep.getDisabled() == 1) {
                continue;
            }

            JSONObject childStepJsonObj = handleSteps(childStep);

            childStepJsonObjs.add(childStepJsonObj);
        }
        stepsJsonObj.put("childSteps", childStepJsonObjs);

        return stepsJsonObj;
    }

    @Override
    public boolean delete(int id) {
        // 先删除 test_suites_test_cases 以及 test_suites_devices 两表中的记录
        testSuitesTestCasesMapper.delete(new LambdaQueryWrapper<TestSuitesTestCases>()
                .eq(TestSuitesTestCases::getTestSuitesId, id));
        testSuitesDevicesMapper.delete(new LambdaQueryWrapper<TestSuitesDevices>()
                .eq(TestSuitesDevices::getTestSuitesId, id));
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTestSuites(TestSuitesDTO testSuitesDTO) {
        TestSuites testSuites = testSuitesDTO.convertTo();
        save(testSuites);

        Integer suiteId = testSuites.getId();
        testSuitesDTO.setId(suiteId);

        List<TestCasesDTO> testCases = testSuitesDTO.getTestCases();
        List<DevicesDTO> devices = testSuitesDTO.getDevices();

        // 删除旧数据
        testSuitesDevicesMapper.delete(new LambdaQueryWrapper<TestSuitesDevices>()
                .eq(TestSuitesDevices::getTestSuitesId, suiteId)
        );
        testSuitesTestCasesMapper.delete(new LambdaQueryWrapper<TestSuitesTestCases>()
                .eq(TestSuitesTestCases::getTestSuitesId, suiteId)
        );

        // 保存testcase映射
        for (int i = 0; i < testCases.size(); i++) {
            testSuitesTestCasesMapper.insert(
                    new TestSuitesTestCases()
                            .setTestSuitesId(suiteId)
                            .setTestCasesId(testCases.get(i).getId())
                            .setSort(i + 1)
            );
        }

        // 保存devices映射
        for (int i = 0; i < devices.size(); i++) {
            testSuitesDevicesMapper.insert(
                    new TestSuitesDevices()
                            .setTestSuitesId(suiteId)
                            .setDevicesId(devices.get(i).getId())
                            .setSort(i + 1)
            );
        }
    }

    @Override
    @Transactional
    public CommentPage<TestSuitesDTO> findByProjectId(int projectId, String name, Page<TestSuites> pageable) {

        LambdaQueryChainWrapper<TestSuites> lambdaQuery = lambdaQuery();

        if (projectId != 0) {
            lambdaQuery.eq(TestSuites::getProjectId, projectId);
        }
        if (name != null && name.length() > 0) {
            lambdaQuery.like(TestSuites::getName, name);
        }

        lambdaQuery.orderByDesc(TestSuites::getId);
        Page<TestSuites> page = lambdaQuery.page(pageable);

        List<TestSuitesDTO> testSuitesDTOList = page.getRecords()
                .stream().map(e -> findById(e.getId())).collect(Collectors.toList());

        return CommentPage.convertFrom(page, testSuitesDTOList);
    }

    @Override
    public List<TestSuitesDTO> findByProjectId(int projectId) {
        return lambdaQuery().eq(TestSuites::getProjectId, projectId)
                .orderByDesc(TestSuites::getId)
                .list()
                .stream().map(e -> findById(e.getId())).collect(Collectors.toList());
    }

    @Override
    public boolean deleteByProjectId(int projectId) {
        List<TestSuites> testSuitesList = baseMapper.selectList(
                new LambdaQueryWrapper<TestSuites>().eq(TestSuites::getProjectId, projectId));
        if (testSuitesList != null && !testSuitesList.isEmpty()) {
            return testSuitesList.stream()
                    .map(TestSuites::getId)
                    .map(this::delete)
                    .reduce(true, Boolean::logicalAnd);
        } else {
            // 如果查询到的list不会null，但是数量为0，说明本身不存在测试套件，直接返回true。
            return testSuitesList != null;
        }
    }

    @Override
    public List<TestSuites> listTestSuitesByTestCasesId(int testCasesId) {
        return testSuitesTestCasesMapper.listTestSuitesByTestCasesId(testCasesId);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        initCoverHandlerMap();
    }

    private void initCoverHandlerMap() {
        Map<String, CoverHandler> coverHandlerBeans = applicationContext.getBeansOfType(CoverHandler.class);
        coverHandlerMap = new HashMap<>();
        for (CoverHandler coverHandler : coverHandlerBeans.values()) {
            coverHandlerMap.put(coverHandler.cover(), coverHandler);
        }
    }

    private JSONObject packageTestCase(Devices devices, int isOpenPerfmon, int perfmonInterval, TestCasesDTO testCases,
                                       JSONObject gp, Results results, StepsService stepsService) {
        JSONObject testCase = new JSONObject();
        List<JSONObject> steps = new ArrayList<>();
        List<StepsDTO> stepsList = testCases.getSteps();
        if (null == stepsList) {
            stepsList = stepsService.findByCaseIdOrderBySort(testCases.getId(), true);
        }
        for (StepsDTO s : stepsList) {
            steps.add(getStep(s));
        }
//        List<StepsDTO> stepsList = stepsService.findByCaseIdOrderBySort(testCases.getId(), true);
//        for (StepsDTO s : stepsList) {
//            steps.add(getStep(s));
//        }
        JSONObject perf = new JSONObject();
        perf.put("isOpen", isOpenPerfmon);
        perf.put("perfInterval", perfmonInterval);
        testCase.put("perf", perf);
        testCase.put("steps", steps);
        testCase.put("cid", testCases.getId());
        testCase.put("device", new ArrayList<>() {{
            add(devices);
        }});
        testCase.put("gp", gp);
        testCase.put("rid", results.getId());
        return testCase;
    }

    /**
     * 更新全局变量，如果变量为多值以设备维度进行分配
     * 当设备数量大于变量数量时，前面设备按顺序分配变
     * 量，后面设备统一取变量的最后一个值。
     *
     * @param gp
     * @param valueMap
     * @return
     */
    private JSONObject refreshGlobalParams(JSONObject gp, Map<String, List<String>> valueMap) {
        boolean needClone = true;
        for (String k : valueMap.keySet()) {
            if (valueMap.get(k).size() > 0) {
                String v = valueMap.get(k).get(0);
                if (needClone && gp.get(k) != null) {
                    gp = (JSONObject) gp.clone();
                    needClone = false;
                }
                gp.put(k, v);
                valueMap.get(k).remove(0);
            } else {
                valueMap.remove(k);
            }
        }
        return gp;
    }

    interface CoverHandler {
        void handlerSuite(TestSuitesDTO testSuitesDTO, JSONObject gp, List<Devices> devicesList,
                          Map<String, List<String>> valueMap, Results results);

        Integer cover();
    }

    /**
     * 用例覆盖处理器
     */
    @Service
    class CaseCoverHandler implements CoverHandler {
        @Autowired
        private StepsService stepsService;

        @Override
        public void handlerSuite(TestSuitesDTO testSuitesDTO, JSONObject gp,
                                 List<Devices> devicesList, Map<String, List<String>> valueMap, Results results) {
            List<JSONObject> suiteDetailList = new ArrayList<>();
            for (int i = 0; i < devicesList.size(); i++) {
                Devices devices = devicesList.get(i);
                gp = refreshGlobalParams(gp, valueMap);
                for (int j = i; j < testSuitesDTO.getTestCases().size(); j += devicesList.size()) {
                    TestCasesDTO testCases = testSuitesDTO.getTestCases().get(j);
                    suiteDetailList.add(packageTestCase(devices, testSuitesDTO.getIsOpenPerfmon(), testSuitesDTO.getPerfmonInterval(),
                            testCases, gp, results, this.stepsService));
                }
                send(devices.getAgentId(), testSuitesDTO.getPlatform(), suiteDetailList);
                suiteDetailList.clear();
            }
        }

        @Override
        public Integer cover() {
            return CoverType.CASE;
        }
    }

    /**
     * 设备覆盖处理器
     */
    @Service
    class DeviceCoverHandler implements CoverHandler {
        @Autowired
        private StepsService stepsService;

        @Autowired
        AccountsService accountsService;

        @Override
        public void handlerSuite(TestSuitesDTO testSuitesDTO, JSONObject gp,
                                 List<Devices> devicesList, Map<String, List<String>> valueMap, Results results) {
            List<JSONObject> suiteDetailList = null;
            Accounts accounts = null;
            String context = "";
            List<Accounts> accountsList = (List<Accounts>) testSuitesDTO.getSupper().getOrDefault("accounts", null);
            for (int deviceIndex = 0; deviceIndex < devicesList.size(); deviceIndex++) {
                Devices devices = devicesList.get(deviceIndex);
                if (null != accountsList) {
                    accounts = accountsList.get(deviceIndex);
                }
                gp = refreshGlobalParams(gp, valueMap);
//                if (suiteDetailList == null) {
                suiteDetailList = new ArrayList<>();
                for (TestCasesDTO testCases : testSuitesDTO.getTestCases()) {
                    // 如果是登陆用例 那么就获取帐号
                    if (null != accountsList) {
                        for (StepsDTO step : testCases.getSteps()) {
                            context = StringUtils.formatV(step.getContent(), (JSONObject) JSONUtils.toJSON(accounts));
                            log.info("content:{} >> >> {}", step.getContent(), context);
                            step.setContent(context);
                        }
                    } else {
                        for (StepsDTO step : testCases.getSteps()) {
                            context = StringUtils.formatV(step.getContent(), testSuitesDTO.getSupper());
                            log.info("content:{} >>> {}", step.getContent(), context);
                            step.setContent(context);
                        }
                    }
                    suiteDetailList.add(packageTestCase(devices, testSuitesDTO.getIsOpenPerfmon(), testSuitesDTO.getPerfmonInterval(),
                            testCases, gp, results, this.stepsService));
                }
//                } else {
//                    for (JSONObject suiteDetail : suiteDetailList) {
//                        suiteDetail.put("device", new ArrayList<>() {{
//                            add(devices);
//                        }});
//                        suiteDetail.put("gp", gp);
//                    }
//                }
                send(devices.getAgentId(), testSuitesDTO.getPlatform(), suiteDetailList);
                if (null != accounts) {
                    accountsService.updateUdId(accounts.getName(), devices.getUdId());
                }
            }
        }

        @Override
        public Integer cover() {
            return CoverType.DEVICE;
        }
    }

    /**
     * 封装数据并发送执行机
     *
     * @param agentId
     * @param platform
     * @param suiteDetailList
     */
    private void send(Integer agentId, Integer platform, List<JSONObject> suiteDetailList) {
        JSONObject result = new JSONObject();
        result.put("msg", "suite");
        result.put("pf", platform);
        result.put("cases", suiteDetailList);
        TransportWorker.send(agentId, result);
    }
}
