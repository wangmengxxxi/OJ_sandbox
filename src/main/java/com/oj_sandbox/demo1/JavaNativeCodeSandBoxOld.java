package com.oj_sandbox.demo1;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.oj_sandbox.demo1.model.ExecuteCodeRequest;
import com.oj_sandbox.demo1.model.ExecuteCodeResponse;
import com.oj_sandbox.demo1.model.ExecuteMessage;
import com.oj_sandbox.demo1.model.JudgeInfo;
import com.oj_sandbox.demo1.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaNativeCodeSandBoxOld implements CodeSandBox {

    private  static final String GLOBAL_CODE_PATH_NAME = "tmpCode";
    private static final String GLOBAL_Java_Class_Name="Main.java";
    private static final Charset PROCESS_CHARSET = StandardCharsets.UTF_8;

    private static final long TIME_OUT=5000L;

    private static final List<String> blackList = Arrays.asList("Files", "exec");

    private static final WordTree WORD_TREE;

    static {
        //初始化字典树
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blackList);
    }
    public static void main(String[] args) {
        JavaNativeCodeSandBoxOld javaNativeCodeSandBox = new JavaNativeCodeSandBoxOld();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
//        String code = ResourceUtil.readStr("testCode/simpleComPuteArgs/Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/simpleComPute/Main.java", StandardCharsets.UTF_8);
//            String code =ResourceUtil.readStr("testCode/unsafeCode/SleepError.java",StandardCharsets.UTF_8);
//        String code =ResourceUtil.readStr("testCode/unsafeCode/MemoryError.java",StandardCharsets.UTF_8);
        String code =ResourceUtil.readStr("testCode/unsafeCode/ReadFileError.java",StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        executeCodeRequest.setInputList(Arrays.asList("1 2","1 3"));
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandBox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);


    }
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        //校验代码是否合理
        //使用字典树
//        WordTree wordTree =new WordTree();
        WORD_TREE.addWords(blackList);
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if(foundWord!=null){
            System.out.println("代码中包含黑名单单词："+foundWord.getFoundWord());
            return null;
        }
        //1.把用户代码保存为文件
        String userDir = System.getProperty("user.dir");
        String globalCodePathName=userDir+ File.separator+ GLOBAL_CODE_PATH_NAME;// 全局代码文件路径
        //判断全局代码文件是否存在
        if(FileUtil.exist( globalCodePathName)){
            FileUtil.mkdir( globalCodePathName);
        }
        //所有代码都是main，因此对每一次提交的都要分级
        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath=userCodeParentPath+File.separator+ GLOBAL_Java_Class_Name;//存代码文件路径
        //写代码到文件中
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        System.out.println("代码写入文件成功");
        //2.编译代码获得.class文件
        try {
            Process compileprocess = new ProcessBuilder("javac", "-encoding", "utf-8", userCodeFile.getAbsolutePath()).start();
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileprocess, "编译", Charset.defaultCharset());
            System.out.println(executeMessage);
        } catch (IOException e) {
            return getErrorResponse(e);

        }

        //3. 执行代码获取结果
        //主要注意ProcessUtil的实现

        List<ExecuteMessage> executeMessagesList = new ArrayList<>();
        long maxtime=0L;
        for (String inputAgs:inputList
        ) {
            try {

                List<String> runCommand = new ArrayList<>();
                runCommand.add("java");
                runCommand.add("-Xmx256m");
                runCommand.add("-Dfile.encoding=UTF-8");
                runCommand.add("-Dsun.stdout.encoding=UTF-8");
                runCommand.add("-Dsun.stderr.encoding=UTF-8");
                runCommand.add("-cp");
                runCommand.add(userCodeParentPath);
                runCommand.add("Main");
                runCommand.addAll(Arrays.asList(inputAgs.split(" ")));

                Process runProcess = new ProcessBuilder(runCommand).start();
                //超时控制
                new Thread(()->{
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时了，中断");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行", PROCESS_CHARSET);
//                ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, inputAgs, PROCESS_CHARSET);
                System.out.println(executeMessage);
                executeMessagesList.add(executeMessage);
                Long time= executeMessage.getTime();
                if(time!=null){
                    maxtime=Math.max(time,maxtime);
                }
            } catch (Exception e) {
                return getErrorResponse(e);
            }
        }
            // 4.整理输出
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();


            List<String> outputList =new ArrayList<>();
            for (ExecuteMessage executeMessage:
                 executeMessagesList) {
                String errorMessage = executeMessage.getErrorMessage();
                if(StrUtil.isNotBlank(errorMessage)){
                        executeCodeResponse.setMessage(errorMessage);
                }
                outputList.add(executeMessage.getMessage());
            }
            //程序正常运行结束
            JudgeInfo judgeInfo=new JudgeInfo();

            //内存的读取要借助第三方库来实现，麻烦
//            judgeInfo.setMemory();
            judgeInfo.setTime(maxtime);

            if(outputList.size()==executeMessagesList.size()){
                executeCodeResponse.setStatus(1);
            }

            executeCodeResponse.setJudgeInfo(judgeInfo);

            //5.文件清理

        if(userCodeFile.getParentFile()!=null){
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除"+(del?"成功":"失败"));

        }
            //6.编写错误处理，提升程序健壮性


        return executeCodeResponse;
    }

    /**
     * 获取错误响应
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Exception e){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        //2代表代码沙箱有错误
        executeCodeResponse.setStatus(2);

        executeCodeResponse.setJudgeInfo(new JudgeInfo());

        executeCodeResponse.setMessage(e.getMessage());
        return executeCodeResponse;
    }
}
