/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.enums;

/**
 *
 * @author gpatitakis
 */
public enum UserTypeEnum {
    SUPER_ADMIN(1),
    ELECTRICIAN(2),
    ENGINEER(3),
    DEPARTMENT_ADMIN(4),
    OPERATOR(5);

    private final Integer id;

    private UserTypeEnum(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
