package com.oj_sandbox.demo1.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.time.Duration;

public class DockerDemo{
    public static void main(String[] args){
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        PingCmd pingCmd= dockerClient.pingCmd();
        pingCmd.exec();
    }

}
