package org.jk.entity;

public class TransactionRequestLogs extends BaseLogs{
    private int id;
    private String trace_id;
    private String project_name;
    private String in_param;
    private String out_param;
    private int status;
    private String group_name;
    private String feign_client_name;
    private int sort;
    private String parent_node;
    private String local_node;

    @Override
    public String toString() {
        return "TransactionRequestLogs{" +
                "id=" + id +
                ", trace_id='" + trace_id + '\'' +
                ", project_name='" + project_name + '\'' +
                ", in_param='" + in_param + '\'' +
                ", out_param='" + out_param + '\'' +
                ", status=" + status +
                ", group_name='" + group_name + '\'' +
                ", feign_client_name='" + feign_client_name + '\'' +
                '}';
    }

    public String getParent_node() {
        return parent_node;
    }

    public void setParent_node(String parent_node) {
        this.parent_node = parent_node;
    }

    public String getLocal_node() {
        return local_node;
    }

    public void setLocal_node(String local_node) {
        this.local_node = local_node;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
