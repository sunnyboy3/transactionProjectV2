package org.jk.entity;

public class AddTransactionRequestLogs {
    private String trace_id;
    private String project_name;
    private String in_param;
    private String out_param;
    private Integer status;
    private String group_name;
    private String feign_client_name;

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getFeign_client_name() {
        return feign_client_name;
    }

    public void setFeign_client_name(String feign_client_name) {
        this.feign_client_name = feign_client_name;
    }

    public String getTrace_id() {
        return trace_id;
    }

    public void setTrace_id(String trace_id) {
        this.trace_id = trace_id;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getIn_param() {
        return in_param;
    }

    public void setIn_param(String in_param) {
        this.in_param = in_param;
    }

    public String getOut_param() {
        return out_param;
    }

    public void setOut_param(String out_param) {
        this.out_param = out_param;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
