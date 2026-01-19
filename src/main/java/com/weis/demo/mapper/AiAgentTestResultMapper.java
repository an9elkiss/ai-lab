package com.weis.demo.mapper;

import com.weis.demo.entity.AiAgentTestResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能体测试结果 Mapper 接口
 *
 * @author weis
 * @date 2025-01-19
 */
@Mapper
public interface AiAgentTestResultMapper {

    /**
     * 插入测试结果
     *
     * @param testResult 测试结果
     * @return 影响行数
     */
    int insert(AiAgentTestResult testResult);

    /**
     * 根据ID删除测试结果
     *
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据用例ID删除测试结果
     *
     * @param caseId 测试用例ID
     * @return 影响行数
     */
    int deleteByCaseId(@Param("caseId") Long caseId);

    /**
     * 更新测试结果
     *
     * @param testResult 测试结果
     * @return 影响行数
     */
    int update(AiAgentTestResult testResult);

    /**
     * 根据ID查询测试结果
     *
     * @param id 主键ID
     * @return 测试结果
     */
    AiAgentTestResult selectById(@Param("id") Long id);

    /**
     * 查询所有测试结果
     *
     * @return 测试结果列表
     */
    List<AiAgentTestResult> selectAll();

    /**
     * 根据用例ID查询测试结果
     *
     * @param caseId 测试用例ID
     * @return 测试结果列表
     */
    List<AiAgentTestResult> selectByCaseId(@Param("caseId") Long caseId);

    /**
     * 根据执行状态查询
     *
     * @param executionStatus 执行状态
     * @return 测试结果列表
     */
    List<AiAgentTestResult> selectByExecutionStatus(@Param("executionStatus") String executionStatus);

    /**
     * 根据测试环境查询
     *
     * @param testEnvironment 测试环境
     * @return 测试结果列表
     */
    List<AiAgentTestResult> selectByTestEnvironment(@Param("testEnvironment") String testEnvironment);

    /**
     * 根据智能体版本查询
     *
     * @param agentVersion 智能体版本号
     * @return 测试结果列表
     */
    List<AiAgentTestResult> selectByAgentVersion(@Param("agentVersion") String agentVersion);

    /**
     * 根据模型版本查询
     *
     * @param modelVersion 模型版本号
     * @return 测试结果列表
     */
    List<AiAgentTestResult> selectByModelVersion(@Param("modelVersion") String modelVersion);

    /**
     * 查询用例最新的测试结果
     *
     * @param caseId 测试用例ID
     * @return 最新的测试结果
     */
    AiAgentTestResult selectLatestByCaseId(@Param("caseId") Long caseId);
}
