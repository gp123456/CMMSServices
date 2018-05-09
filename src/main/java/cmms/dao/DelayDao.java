/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.dao;

import cmms.models.Delay;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author gpatitakis
 */
@Transactional
public interface DelayDao extends JpaRepository<Delay, Long> {

    /**
     * Return the all for specific list department.
     *
     * @param department the department id(s).
     * @return the db delay info or null
     * @throws java.lang.Exception
     */
    public List findByDepartmentInOrderByDescriptionAsc(List department) throws Exception;

    /**
     * Return the all delay(s) for a specific department.
     *
     * @param department the department id.
     * @return the delay id(s) or null
     * @throws java.lang.Exception
     */
    public List<Delay> findByDepartment(Long department) throws Exception;

    public Delay findByIdAndDepartment(Long id, Long Department) throws Exception;

    /**
     * Return all departments of delays.
     *
     * @return the departments with counter per department
     * @throws java.lang.Exception
     */
    @Query("SELECT d.department FROM Delay d GROUP BY d.department")
    public List<Long> findAllDepartments() throws Exception;
} // class DelayDao
