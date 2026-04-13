package com.oj_sandbox.demo1;

import com.oj_sandbox.demo1.model.ExecuteCodeRequest;
import com.oj_sandbox.demo1.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

@Component
/***
 * Java原生实现，直接继承复用
 */
public class JavaNativeCodeSandBox extends JavaCodeSandBoxTemPlate {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
