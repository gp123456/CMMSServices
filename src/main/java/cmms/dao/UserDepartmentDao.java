/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.dao;

import cmms.models.UserDepartment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author gpatitakis
 */
public interface UserDepartmentDao extends JpaRepository<UserDepartment, Long> {

    /**
     * Return the department(s) having the passed user or null if no department is found.
     *
     * @param users the user id.
     * @return the db department info or null
     * @throws java.lang.Exception
     */
    public List findByUsers(String users) throws Exception;
    /**
     * Return the user(s) having the passed department or null if no department is found.
     *
     * @param department the department id.
     * @return the db department info or null
     * @throws java.lang.Exception
     */
    public List findByDepartment(Long department) throws Exception;
} // class UserDepartment
