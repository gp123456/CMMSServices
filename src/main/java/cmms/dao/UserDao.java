package cmms.dao;

import cmms.models.User;
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
public interface UserDao extends JpaRepository<User, String> {

    /**
     * Return the users having the passed type or null if no user is found.
     *
     * @param type the type of user.
     * @return the db user info or null
     * @throws java.lang.Exception
     */
    public List findByTypeOrderByNameAsc(Integer type) throws Exception;
    
    /**
     * Return the users having the passed type or null if no user is found.
     *
     * @param id the ids of user.
     * @param type the type of user.
     * @return the db user info or null
     * @throws java.lang.Exception
     */
    public List findByIdInAndTypeOrderByNameAsc(List id, Integer type) throws Exception;

    /**
     * Return the user having the passed username, password or null if no user is found.
     *
     * @param username the user name.
     * @param password the user password
     * @return the db user info or null
     * @throws java.lang.Exception
     */
    public User findByUsernameAndPassword(String username, String password) throws Exception;
} // class UserDao
