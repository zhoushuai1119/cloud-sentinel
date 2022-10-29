package com.cloud.sentinel.dashboard.service.impl;

import com.cloud.sentinel.dashboard.datasource.entity.Metric;
import com.cloud.sentinel.dashboard.mapper.MetricMapper;
import com.cloud.sentinel.dashboard.service.MetricService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Sentinel监控信息表 服务实现类
 * </p>
 *
 * @author zhoushuai
 * @since 2022-10-20
 */
@Service
public class MetricServiceImp extends ServiceImpl<MetricMapper, Metric> implements MetricService {

}
