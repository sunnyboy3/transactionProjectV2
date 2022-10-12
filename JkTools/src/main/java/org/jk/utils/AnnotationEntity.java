package org.jk.utils;

public class AnnotationEntity {
    private String name;
    private String groupName;
    private String feignClientName;

    public AnnotationEntity(String name, String groupName,String feignClientName) {
        this.name = name;
        this.groupName = groupName;
        this.feignClientName = feignClientName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getFeignClientName() {
        return feignClientName;
    }

    public void setFeignClientName(String feignClientName) {
        this.feignClientName = feignClientName;
    }
}
