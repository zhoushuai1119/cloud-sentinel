package com.cloud.sentinel.dashboard.service;

import com.cloud.sentinel.dashboard.discovery.AppInfo;
import com.cloud.sentinel.dashboard.discovery.AppManagement;
import com.cloud.sentinel.dashboard.discovery.MachineInfo;
import com.cloud.sentinel.dashboard.domain.vo.MachineInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @description: 获取服务机器列表
 * @author: zhou shuai
 * @date: 2022/10/18 16:08
 * @version: v1
 */
@Service
public class ClusterMachineService {

    @Autowired
    private AppManagement appManagement;

    public MachineInfoVo getMachinesByApp(String app) {
        AppInfo appInfo = appManagement.getDetailApp(app);
        if (appInfo == null) {
            return null;
        }
        List<MachineInfo> list = new ArrayList<>(appInfo.getMachines());
        Collections.sort(list, Comparator.comparing(MachineInfo::getApp).thenComparing(MachineInfo::getIp).thenComparingInt(MachineInfo::getPort));
        List<MachineInfoVo> machineInfoVoList = MachineInfoVo.fromMachineInfoList(list);
        if (!CollectionUtils.isEmpty(machineInfoVoList)) {
            for (MachineInfoVo machineInfoVo : machineInfoVoList) {
                if (machineInfoVo.isHealthy()) {
                    return machineInfoVo;
                }
            }
        }
        return null;
    }

}
