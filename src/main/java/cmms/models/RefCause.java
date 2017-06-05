/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.models;

import java.io.Serializable;

/**
 *
 * @author gpatitakis
 */
public class RefCause implements Serializable {

    private Long departmentId;
    private String department;
    private String type;
    private String cause;
    private Long causeId;
    private String subcause;
    private String subcauseCode;
    private Long subcauseId;

    public RefCause() {
    }

    public RefCause(Long departmentId, String department, String type, Long causeId, String cause, String subcause, Long subcauseId) {
        this.departmentId = departmentId;
        this.department = department;
        this.type = type;
        this.causeId = causeId;
        this.cause = cause;
        this.subcause = subcause;
        this.subcauseId = subcauseId;
    }

    public RefCause(String department, String type, String cause, Long causeId) {
        this.department = department;
        this.type = type;
        this.cause = cause;
        this.causeId = causeId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCauseId() {
        return causeId;
    }

    public void setCauseId(Long causeId) {
        this.causeId = causeId;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getSubcause() {
        return subcause;
    }

    public void setSubcause(String subcause) {
        this.subcause = subcause;
    }

    public Long getSubcauseId() {
        return subcauseId;
    }

    public void setSubcauseId(Long subcauseId) {
        this.subcauseId = subcauseId;
    }

    public String getSubcauseCode() {
        return subcauseCode;
    }

    public void setSubcauseCode(String subcauseCode) {
        this.subcauseCode = subcauseCode;
    }
}
