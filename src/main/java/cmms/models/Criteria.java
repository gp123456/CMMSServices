/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author gpatitakis
 */
public class Criteria implements Serializable {

    // ------------------------
    // PRIVATE FIELDS
    // ------------------------
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    private Date from;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    private Date to;

    private Long[] departments;

    private Long[] machines;

    private Long[] types;

    private Long[] causes;

    private Long[] subcauses;

    private String[] users;

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    public Criteria() {
    }

    // Getter and setter methods
    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public Long[] getDepartments() {
        return departments;
    }

    public void setDepartments(Long[] departments) {
        this.departments = departments;
    }

    public Long[] getMachines() {
        return machines;
    }

    public void setMachines(Long[] machines) {
        this.machines = machines;
    }

    public Long[] getTypes() {
        return types;
    }

    public void setTypes(Long[] types) {
        this.types = types;
    }

    public Long[] getCauses() {
        return causes;
    }

    public void setCauses(Long[] causes) {
        this.causes = causes;
    }

    public Long[] getSubcauses() {
        return subcauses;
    }

    public void setSubcauses(Long[] subcauses) {
        this.subcauses = subcauses;
    }

    public String[] getUsers() {
        return users;
    }

    public void setUsers(String[] users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return
                "from=" + from + "," +
                "to=" + to + "," +
                "departments=" + ((departments.length > 0) ? Arrays.toString(departments) : "") + "," +
                "machines=" + ((machines.length > 0) ? Arrays.toString(machines) : "") + "," +
                "types=" + ((types.length > 0) ? Arrays.toString(types) : "") + "," +
                "causes=" + ((causes.length > 0) ? Arrays.toString(causes) : "") + "," +
                "subcauses=" + ((subcauses.length > 0) ? Arrays.toString(subcauses) : "") + "," +
                "users=" + ((users.length > 0) ? Arrays.toString(users) : "");
    }
} // class Criteria
