/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.models;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author gpatitakis
 */
public class DepartmentPareto {

    private Long id;

    private String department;

    private List<Pareto> paretos;

    private BigDecimal mttr;

    private BigDecimal mtbf;

    private String machineCodes;

    public DepartmentPareto(Long id, String department, List paretos, BigDecimal mttr, BigDecimal mtbf, String machineCodes) {
        this.id = id;
        this.department = department;
        this.paretos = paretos;
        this.mttr = mttr;
        this.mtbf = mtbf;
        this.machineCodes = machineCodes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<Pareto> getParetos() {
        return paretos;
    }

    public void setParetos(List<Pareto> paretos) {
        this.paretos = paretos;
    }

    public BigDecimal getMttr() {
        return mttr;
    }

    public void setMttr(BigDecimal mttr) {
        this.mttr = mttr;
    }

    public BigDecimal getMtbf() {
        return mtbf;
    }

    public void setMtbf(BigDecimal mtbf) {
        this.mtbf = mtbf;
    }

    public String getMachineCodes() {
        return machineCodes;
    }

    public void setMachineCodes(String machineCodes) {
        this.machineCodes = machineCodes;
    }
}
