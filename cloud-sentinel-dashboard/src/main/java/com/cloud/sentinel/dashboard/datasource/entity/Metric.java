package com.cloud.sentinel.dashboard.datasource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * Sentinel监控信息表
 * </p>
 *
 * @author zhoushuai
 * @since 2022-10-20
 */
@TableName("metric")
public class Metric {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 应用名称
     */
    @TableField("app")
    private String app;
    /**
     * 资源名称
     */
    @TableField("resource")
    private String resource;
    /**
     * 监控信息时间戳
     */
    @TableField("timestamp")
    private Date timestamp;
    /**
     * 创建时间
     */
    @TableField("gmt_create")
    private Date gmtCreate;
    /**
     * 修改时间
     */
    @TableField("gmt_modified")
    private Date gmtModified;
    /**
     * 通过QPS
     */
    @TableField("pass_qps")
    private Long passQps;
    /**
     * 成功QPS
     */
    @TableField("success_qps")
    private Long successQps;
    /**
     * 限流QPS
     */
    @TableField("block_qps")
    private Long blockQps;
    /**
     * 异常QPS
     */
    @TableField("exception_qps")
    private Long exceptionQps;
    /**
     * 资源的平均响应时间
     */
    @TableField("rt")
    private BigDecimal rt;
    /**
     * 本次聚合的总条数
     */
    @TableField("count")
    private Integer count;
    /**
     * 资源hashcode
     */
    @TableField("resource_code")
    private Integer resourceCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public Long getPassQps() {
        return passQps;
    }

    public void setPassQps(Long passQps) {
        this.passQps = passQps;
    }

    public Long getSuccessQps() {
        return successQps;
    }

    public void setSuccessQps(Long successQps) {
        this.successQps = successQps;
    }

    public Long getBlockQps() {
        return blockQps;
    }

    public void setBlockQps(Long blockQps) {
        this.blockQps = blockQps;
    }

    public Long getExceptionQps() {
        return exceptionQps;
    }

    public void setExceptionQps(Long exceptionQps) {
        this.exceptionQps = exceptionQps;
    }

    public BigDecimal getRt() {
        return rt;
    }

    public void setRt(BigDecimal rt) {
        this.rt = rt;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(Integer resourceCode) {
        this.resourceCode = resourceCode;
    }
}
