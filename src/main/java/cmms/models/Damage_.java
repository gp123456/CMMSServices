/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.models;

import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

/**
 *
 * @author gpatitakis
 */
@StaticMetamodel(Damage.class)
public class Damage_ {

    public static volatile SingularAttribute<Damage, Integer> id;
    public static volatile SingularAttribute<Damage, Date> created;
    public static volatile SingularAttribute<Damage, String> user;
    public static volatile SingularAttribute<Damage, Long> department;
    public static volatile SingularAttribute<Damage, Long> type;
    public static volatile SingularAttribute<Damage, Long> cause;
    public static volatile SingularAttribute<Damage, Long> machine;
    public static volatile SingularAttribute<Damage, Long> duration;
    public static volatile SingularAttribute<Damage, String> note;
    public static volatile SingularAttribute<Damage, Boolean> deleted;
}
