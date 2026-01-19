package com.weis.demo.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONUtil;
import com.weis.demo.dto.AiAgentTestResultDTO;
import com.weis.demo.dto.command.AiAgentTestCaseFindCmd;
import com.weis.demo.entity.AiAgentTestCase;
import com.weis.demo.entity.AiAgentTestResult;
import com.weis.demo.mapper.AiAgentTestCaseMapper;
import com.weis.demo.mapper.AiAgentTestResultMapper;
import com.weis.demo.service.test.AgentTestHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentTestService {

    private final AiAgentTestCaseMapper aiAgentTestCaseMapper;

    private final AiAgentTestResultMapper aiAgentTestResultMapper;

    private final ApplicationContext applicationContext;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public void test(AiAgentTestCaseFindCmd cmd) {
        List<AiAgentTestCase> testCases = aiAgentTestCaseMapper.findByCondition(cmd);
        log.warn("查询到测试用例数量: {}, 详情: {}", testCases.size(), JSONUtil.toJsonStr(testCases));

        List<AiAgentTestResultDTO> testResults = new ArrayList<>();
        // testCases每个元素构建成AiAgentTestResult对象，并保存到数据库中
        for (AiAgentTestCase testCase : testCases) {
            AiAgentTestResult testResult = new AiAgentTestResult();
            testResult.setCaseId(testCase.getId());
            testResult.setExecutionStatus("testing");
            testResult.setExecutionTime(new Date());
            
            int insertCount = aiAgentTestResultMapper.insert(testResult);
            log.warn("保存测试结果: caseId={}, executionStatus={}, insertCount={}", 
                    testCase.getId(), "testing", insertCount);

            AiAgentTestResultDTO dto = new AiAgentTestResultDTO();
            BeanUtil.copyProperties(testResult, dto);
            dto.setAiAgentTestCase(testCase);
            testResults.add(dto);
        }

        // 异步调用executeTestCase逐条执行testCases
        for (AiAgentTestResultDTO testResult : testResults) {
            executeTestCase(testResult);
        }

        log.warn("批量创建测试结果完成，共创建 {} 条记录，已提交异步执行任务", testCases.size());
    }

    /**
     * 异步执行单个测试用例
     *
     */
    @Async("testExecutor")
    public void executeTestCase(AiAgentTestResultDTO testResult) {
        AiAgentTestCase testCase = testResult.getAiAgentTestCase();
        log.warn("开始异步执行测试用例: caseId={}, caseName={}", testCase.getId(), testCase.getCaseName());
        
        long startTime = System.currentTimeMillis();
        
        try {
            String handler = testCase.getHandler();
            
            // 获取name = handler的 bean
            if (handler == null || handler.trim().isEmpty()) {
                log.error("测试用例handler为空: caseId={}", testCase.getId());
                throw new RuntimeException("测试用例handler为空: caseId=" + testCase.getId());
            }
            
            AgentTestHandler handlerBean = applicationContext.getBean(handler, AgentTestHandler.class);
            log.warn("获取到handler bean: handler={}, caseId={}", handler, testCase.getId());
            
            // 调用handler处理测试用例
            handlerBean.handle(testResult);

            // 设置execution_duration，execution_status，test_environment
            long endTime = System.currentTimeMillis();
            int executionDuration = (int) (endTime - startTime) / 1000;
            testResult.setExecutionDuration(executionDuration);
            testResult.setExecutionStatus("success");
            testResult.setTestEnvironment(activeProfile);
            
            // 更新测试结果到数据库
            int updateCount = aiAgentTestResultMapper.update(testResult);
            log.warn("更新测试结果: caseId={}, executionDuration={}ms, executionStatus={}, updateCount={}", 
                    testCase.getId(), executionDuration, "success", updateCount);

            log.warn("测试用例执行完成: caseId={}", testCase.getId());
        } catch (Exception e) {
            // 设置execution_duration，execution_status，test_environment，error_message
            long endTime = System.currentTimeMillis();
            int executionDuration = (int) (endTime - startTime)/1000;
            testResult.setExecutionDuration(executionDuration);
            testResult.setExecutionStatus("failed");
            testResult.setTestEnvironment(activeProfile);
            
            // 获取完整的异常堆栈信息并截取前1000字符
            String stackTrace = ExceptionUtil.stacktraceToString(e);
            String errorMessage = stackTrace.length() > 1000 ? stackTrace.substring(0, 1000) : stackTrace;
            testResult.setErrorMessage(errorMessage);
            
            // 更新测试结果到数据库
            int updateCount = aiAgentTestResultMapper.update(testResult);
            log.warn("更新失败测试结果: caseId={}, executionDuration={}ms, executionStatus={}, updateCount={}", 
                    testCase.getId(), executionDuration, "failed", updateCount);

            log.error("测试用例执行异常: caseId={}, error={}", testCase.getId(), e.getMessage(), e);
        }
    }
}
