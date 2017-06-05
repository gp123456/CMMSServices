/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.dao;

import cmms.models.Delay;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

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
     * @return the db causes info or null
     * @throws java.lang.Exception
     */
    public List findByDepartmentInOrderByDescriptionAsc(List department) throws Exception;
} // class DelayDao
