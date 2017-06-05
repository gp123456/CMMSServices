package cmms.dao;

import cmms.models.Cause;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A DAO for the entity User is simply created by extending the CrudRepository interface provided by spring. The following methods are some of the
 * ones available from such interface: save, delete, deleteAll, findOne and findAll. The magic is that such methods must not be implemented, and
 * moreover it is possible create new query methods working only by defining their signature!
 *
 * @author netgloo
 */
public interface CauseDao extends JpaRepository<Cause, Long> {

    /**
     * Return the cause where are enabled for specific department and description.
     *
     * @param department the department id.
     * @param description the description cause.
     * @return the db causes info or null
     * @throws java.lang.Exception
     */
    public Cause findByDepartmentAndDescription(Long department, String description) throws Exception;
    
    /**
     * Return the all causes where are enabled for specific department.
     *
     * @param enable equal true.
     * @return the db causes info or null
     * @throws java.lang.Exception
     */
    public List findByEnableOrderByDescriptionAsc(Boolean enable) throws Exception;
    
    /**
     * Return the all causes where are enabled for specific department.
     *
     * @param department the department id.
     * @param enable equal true.
     * @return the db causes info or null
     * @throws java.lang.Exception
     */
    public List findByDepartmentAndEnableOrderByDescriptionAsc(Long department, Boolean enable) throws Exception;
    
    /**
     * Return the causes having the passed department.
     *
     * @param department the department of plant.
     * @param enable equal true.
     * @return the db causes info or null of specific department order by description ascending
     * @throws java.lang.Exception
     */
    public List findByDepartmentInAndEnableOrderByDescriptionAsc(List department, Boolean enable) throws Exception;

    /**
     * Return the causes having the passed type.
     *
     * @param type the types of cause.
     * @param enable equal true.
     * @return the db causes info or null of specific department order by description ascending
     * @throws java.lang.Exception
     */
    public List findByTypeInAndEnableOrderByDescriptionAsc(List type, Boolean enable) throws Exception;

    /**
     * Return the causes having the passed department and type.
     *
     * @param type the types of cause.
     * @param department the departments of plant.
     * @param enable equal true.
     * @return the db cause info or null of specific type and department order by description ascending
     * @throws java.lang.Exception
     */
    public List findByTypeInAndDepartmentInAndEnableOrderByDescriptionAsc(List type, List department, Boolean enable) throws Exception;
    
    /**
     * Return the causes having the passed department and type.
     *
     * @param type the types of cause.
     * @param department the departments of plant.
     * @param enable equal true.
     * @return the db cause info or null of specific type and department order by description ascending
     * @throws java.lang.Exception
     */
    public List findByTypeAndDepartmentInAndEnableOrderByDescriptionAsc(Long type, List department, Boolean enable) throws Exception;
    
    /**
     * Return the causes having the passed department and type.
     *
     * @param type the types of cause.
     * @param department the departments of plant.
     * @param enable equal true.
     * @return the db cause info or null of specific type and department order by description ascending
     * @throws java.lang.Exception
     */
    public List findByTypeAndDepartmentAndEnableOrderByDescriptionAsc(Long type, Long department, Boolean enable) throws Exception;
} // class CauseDao
