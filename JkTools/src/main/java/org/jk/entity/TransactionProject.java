package org.jk.entity;

public class TransactionProject {
    private Long id;
    private String project_name;
    private String transaction_group;
    private String feign_client_name;

    public String getFeign_client_name() {
        return feign_client_name;
    }

    public void setFeign_client_name(String feign_client_name) {
        this.feign_client_name = feign_client_name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getTransaction_group() {
        return transaction_group;
    }

    public void setTransaction_group(String transaction_group) {
        this.transaction_group = transaction_group;
    }
}
