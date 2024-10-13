package org.thanhquang.dto;

import java.util.List;

public class ResDto {

    private String controllerName;
    private String controllerPath;
    private String methodName;
    private String methodType;
    private List<String> methodUrls;

    public ResDto() {
    }

    public ResDto(String controllerName, String controllerPath, String methodName, String methodType, List<String> methodUrls) {
        this.controllerName = controllerName;
        this.controllerPath = controllerPath;
        this.methodName = methodName;
        this.methodType = methodType;
        this.methodUrls = methodUrls;
    }

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    public String getControllerPath() {
        return controllerPath;
    }

    public void setControllerPath(String controllerPath) {
        this.controllerPath = controllerPath;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public List<String> getMethodUrl() {
        return methodUrls;
    }

    public void setMethodUrl(List<String> methodUrls) {
        this.methodUrls = methodUrls;
    }

    public String getMethodUrls() {
        return "[" + String.join("|", methodUrls) + "]";
    }
}
