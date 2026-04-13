package com.oj_sandbox.demo1.controller;

import com.oj_sandbox.demo1.JavaDockerCodeSandBox;
import com.oj_sandbox.demo1.JavaNativeCodeSandBox;
import com.oj_sandbox.demo1.JavaNativeCodeSandBoxOld;
import com.oj_sandbox.demo1.model.ExecuteCodeRequest;
import com.oj_sandbox.demo1.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")
public class MainController {

    private static final String AUTH_REQUEST_HEAD="auth";
    private static final String AUTH_REQUEST_SECRET="secretKey";
    @Resource
    private JavaNativeCodeSandBox javaNativeCodeSandBox;

    @Resource
    private JavaDockerCodeSandBox javaDockerCodeSandBox;
    @GetMapping("/health")
    public String healthcheck() {
        return "OK";
    }

/**
 * 执行代码的接口方法
 * @PostMapping 映射HTTP POST请求到"/executeCode"路径
 * @param executeCodeRequest 包含要执行的代码的请求对象
 * @return ExecuteCodeResponse 执行代码后的响应对象
 */
@PostMapping("/executeCode")
ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest,
                                HttpServletRequest request,
                                HttpServletResponse response) {
    if (executeCodeRequest == null) {
        response.setStatus(403);
        return null;
    }
    String auth = request.getHeader(AUTH_REQUEST_HEAD);
    if (!AUTH_REQUEST_SECRET.equals(auth)) {
        response.setStatus(403);
        return null;
    }
    return javaNativeCodeSandBox.executeCode(executeCodeRequest);
//    return javaDockerCodeSandBox.executeCode(executeCodeRequest);
}

}
