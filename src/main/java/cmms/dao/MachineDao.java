package cmms.dao;

import cmms.models.Machine;
import java.util.Collection;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A DAO for the entity User is simply created by extending the CrudRepository interface provided by spring. The following methods are some of the
 * ones available from such interface: save, delete, deleteAll, findOne and findAll. The magic is that such methods must not be implemented, and
 * moreover it is possible create new query methods working only by defining their signature!
 *
 * @author netgloo
 */
@Transactional
public interface MachineDao extends JpaRepository<Machine, Long> {

    /**
     * Return the machines having the passed department.
     *
     * @param department the department of plant.
     * @return the db machines info or null of specific department
     * @throws java.lang.Exception
     */
    public List findByDepartmentOrderByCodeAsc(Long department) throws Exception;

    List findByDepartmentInOrderByCodeAsc(List department) throws Exception;
} // class MachineDao
