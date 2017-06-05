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
public enum CauseTypeEnum {
    MECHANICAL(1l),
    ELECRTICAL(2l),
    DELAY(3l);

    private final Long id;

    private CauseTypeEnum(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
