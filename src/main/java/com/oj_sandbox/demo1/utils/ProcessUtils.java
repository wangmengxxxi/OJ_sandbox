package com.oj_sandbox.demo1.utils;


import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.StrUtil;
import com.oj_sandbox.demo1.model.ExecuteMessage;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 进程工具类
 */
public class ProcessUtils {

    private static final Charset DEFAULT_PROCESS_CHARSET = StandardCharsets.UTF_8;

    /**
     * 执行进程并获取信息
     *
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
        return runProcessAndGetMessage(runProcess, opName, DEFAULT_PROCESS_CHARSET);
    }

    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName, Charset charset) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 等待程序执行，获取错误码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            // 正常退出
            if (exitValue == 0) {
                System.out.println(opName + "成功");
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream(), charset));
                List<String> outputStrList = new ArrayList<>();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(String.join("\n", outputStrList));
            } else {
                // 异常退出
                System.out.println(opName + "失败，错误码： " + exitValue);
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream(), charset));
                List<String> outputStrList = new ArrayList<>();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(String.join("\n", outputStrList));

                // 分批获取进程的错误输出
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream(), charset));
                // 逐行读取
                List<String> errorOutputStrList = new ArrayList<>();
                // 逐行读取
                String errorCompileOutputLine;
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutputStrList.add(errorCompileOutputLine);
                }
                executeMessage.setErrorMessage(String.join("\n", errorOutputStrList));
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }

    /**
     * 执行交互式进程并获取信息
     *
     * @param runProcess
     * @param args
     * @return
     */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String args) {
        return runInteractProcessAndGetMessage(runProcess, args, DEFAULT_PROCESS_CHARSET);
    }

/**
 * 运行交互式进程并获取执行消息
 * 该方法用于启动一个进程并向其输入命令，然后获取进程的输出结果
 *
 * @param runProcess 要运行的进程对象
 * @param args 要传递给进程的参数字符串，参数之间用空格分隔
 * @param charset 用于处理输入输出的字符编码
 * @return ExecuteMessage 包含执行结果的封装对象
 */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String args, Charset charset) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            // 向控制台输入程序
            // 获取进程的输出流，用于向进程发送输入
            OutputStream outputStream = runProcess.getOutputStream();
            // 使用指定字符集创建输出流写入器
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, charset);
            // 将输入参数按空格分割成数组
            String[] s = args.split(" ");
            // 将参数数组用换行符连接，并在最后添加换行符，模拟按回车操作
            String join = StrUtil.join("\n", s) + "\n";
            // 向进程写入输入内容
            outputStreamWriter.write(join);
            // 刷新输出流，确保内容被发送
            // 相当于按了回车，执行输入的发送
            outputStreamWriter.flush();

            // 分批获取进程的正常输出
            // 获取进程的输入流，用于读取进程的输出
            InputStream inputStream = runProcess.getInputStream();
            // 使用指定字符集创建缓冲读取器
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset));
            // 创建字符串构建器，用于累积进程的输出
            StringBuilder compileOutputStringBuilder = new StringBuilder();
            // 逐行读取进程的输出
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                compileOutputStringBuilder.append(compileOutputLine);
            }
            // 将累积的输出设置到执行消息中
            executeMessage.setMessage(compileOutputStringBuilder.toString());
            // 记得资源的释放，否则会卡死
            // 关闭所有资源并销毁进程
            outputStreamWriter.close();
            outputStream.close();
            inputStream.close();
            runProcess.destroy();
        } catch (Exception e) {
            // 打印异常堆栈信息
            e.printStackTrace();
        }
        return executeMessage;
    }
}
