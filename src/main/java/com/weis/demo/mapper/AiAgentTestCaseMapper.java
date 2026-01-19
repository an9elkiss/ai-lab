package com.weis.demo.mapper;

import com.weis.demo.dto.command.AiAgentTestCaseFindCmd;
import com.weis.demo.entity.AiAgentTestCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能体测试用例 Mapper 接口
 *
 * @author weis
 * @date 2025-01-19
 */
@Mapper
public interface AiAgentTestCaseMapper {

    /**
     * 插入测试用例
     *
     * @param testCase 测试用例
     * @return 影响行数
     */
    int insert(AiAgentTestCase testCase);

    /**
     * 根据ID删除测试用例
     *
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新测试用例
     *
     * @param testCase 测试用例
     * @return 影响行数
     */
    int update(AiAgentTestCase testCase);

    /**
     * 根据ID查询测试用例
     *
     * @param id 主键ID
     * @return 测试用例
     */
    AiAgentTestCase selectById(@Param("id") Long id);

    /**
     * 查询所有测试用例
     *
     * @return 测试用例列表
     */
    List<AiAgentTestCase> selectAll();

    /**
     * 根据用例类型查询
     *
     * @param caseType 用例类型
     * @return 测试用例列表
     */
    List<AiAgentTestCase> selectByCaseType(@Param("caseType") String caseType);

    /**
     * 根据状态查询
     *
     * @param status 状态
     * @return 测试用例列表
     */
    List<AiAgentTestCase> selectByStatus(@Param("status") Integer status);

    /**
     * 根据优先级查询
     *
     * @param priority 优先级
     * @return 测试用例列表
     */
    List<AiAgentTestCase> selectByPriority(@Param("priority") Integer priority);

    /**
     * 根据条件查询测试用例
     *
     * @param cmd 查询条件
     * @return 测试用例列表
     */
    List<AiAgentTestCase> findByCondition(@Param("cmd") AiAgentTestCaseFindCmd cmd);
}
