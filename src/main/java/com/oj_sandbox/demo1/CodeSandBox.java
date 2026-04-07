package com.oj_sandbox.demo1;


import com.oj_sandbox.demo1.model.ExecuteCodeRequest;
import com.oj_sandbox.demo1.model.ExecuteCodeResponse;

public interface CodeSandBox {

    /**
     * 执行代码的方法
     *
     * @param executeCodeRequest 包含要执行的代码及相关参数的请求对象
     * @return ExecuteCodeResponse 执行代码后的响应结果对象
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
