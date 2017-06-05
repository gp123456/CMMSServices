package cmms.models;

import com.google.common.base.Strings;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "vusers")
public class User implements Serializable {

    // ------------------------
    // PRIVATE FIELDS
    // ------------------------
    @Id
    @Column(name = "ABALKY")
    private String id;

    @NotNull
    private String ABALPH;

    @NotNull
    private String ABLOCN;

    @NotNull
    @Column(name = "AB$ROLE")
    private Integer type;

    @NotNull
    @Column(name = "AB$USERNAME")
    private String username;

    @NotNull
    @Column(name = "AB$PASSWORD")
    private String password;
    
    @NotNull
    @Column(name = "ABALPH2")
    private String name;

    @Transient
    private String lastLogin;
    
    @Transient
    private Long[] departments;

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    public User() {
    }

    public User(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getABLOCN() {
        return ABLOCN;
    }

    public void setABLOCN(String ABLOCN) {
        this.ABLOCN = ABLOCN;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getABALPH() {
        return ABALPH;
    }

    public void setABALPH(String ABALPH) {
        this.ABALPH = ABALPH;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    // Getter and setter methods
    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Long[] getDepartments() {
        return departments;
    }

    public void setDepartments(Long[] departments) {
        this.departments = departments;
    }

    public void setChanges(User value) {
        if (value.type != null && value.type.equals(this.type)) {
            this.type = value.type;
        }
        if (!Strings.isNullOrEmpty(value.username) && !value.username.equals(this.username)) {
            this.username = value.username;
        }
        if (!Strings.isNullOrEmpty(value.password) && !value.password.equals(this.password)) {
            this.password = value.password;
        }
        if (!Strings.isNullOrEmpty(value.name) && !value.name.equals(this.name)) {
            this.name = value.name;
        }
    }

    @Override
    public String toString() {
        return "User{"
                + "id=" + id
                + ",type=" + type
                + ",username=" + username
                + ",password=" + password
                + ",name=" + name + '}';
    }
} // class User
