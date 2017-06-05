package cmms.models;

import com.google.common.base.Strings;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

/**
 * An entity User composed by three fields (id, email, name). The Entity annotation indicates that this class is a JPA entity. The Table annotation
 * specifies the name for the table in the db.
 *
 * @author netgloo
 */
@Entity
@Table(name = "vmachine")
public class Machine implements Serializable {

    // ------------------------
    // PRIVATE FIELDS
    // ------------------------
    // An autogenerated id (unique for each user in the db)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "IW$IDFAC")
    private Long id;

    @NotNull
    @Column(name = "IW$CODE")
    private String code;

    @NotNull
    private String IW$FAAL1;

    @NotNull
    private String IW$LINE;

    @NotNull
    @Column(name = "IWDSC1")
    private String description;

    @NotNull
    @Column(name = "IWLOCNKEY")
    private Long department;

    @NotNull
    private String IWLOCN;

    @NotNull
    private String IW$STEP;

    @NotNull
    private Long IWUKIDP;
    
    @NotNull
    private Long IWMCU;

    @Transient
    private String causes;

    @Transient
    private BigDecimal efficiency;

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    public Machine() {
    }

    public Machine(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIW$FAAL1() {
        return IW$FAAL1;
    }

    public void setIW$FAAL1(String IW$FAAL1) {
        this.IW$FAAL1 = IW$FAAL1;
    }

    public String getIW$LINE() {
        return IW$LINE;
    }

    public void setIW$LINE(String IW$LINE) {
        this.IW$LINE = IW$LINE;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDepartment() {
        return department;
    }

    public void setDepartment(Long department) {
        this.department = department;
    }

    public String getIWLOCN() {
        return IWLOCN;
    }

    public void setIWLOCN(String IWLOCN) {
        this.IWLOCN = IWLOCN;
    }

    public String getIW$STEP() {
        return IW$STEP;
    }

    public void setIW$STEP(String IW$STEP) {
        this.IW$STEP = IW$STEP;
    }

    public Long getIWUKIDP() {
        return IWUKIDP;
    }

    public void setIWUKIDP(Long IWUKIDP) {
        this.IWUKIDP = IWUKIDP;
    }

    public Long getIWMCU() {
        return IWMCU;
    }

    public void setIWMCU(Long IWMCU) {
        this.IWMCU = IWMCU;
    }

    public String getCauses() {
        return causes;
    }

    public void setCauses(String causes) {
        this.causes = causes;
    }

    public BigDecimal getEfficiency() {
        return efficiency;
    }

    // Getter and setter methods
    public void setEfficiency(BigDecimal efficiency) {
        this.efficiency = efficiency;
    }

    public void setChanges(Machine value) {
        if (value.department != null && !value.department.equals(this.department)) {
            this.department = value.department;
        }
        if (!Strings.isNullOrEmpty(value.code) && !value.code.equals(this.code)) {
            this.code = value.code;
        }
        if (!Strings.isNullOrEmpty(value.description) && !value.description.equals(this.description)) {
            this.description = value.description;
        }
    }

    @Override
    public String toString() {
        return "Machine{"
                + "id=" + id
                + ",department=" + department
                + ",code=" + code
                + ",desciption=" + description + '}';
    }
} // class Machine
