/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.dao;

import cmms.models.Subcause;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author gpatitakis
 */
@Transactional
public interface SubcauseDao extends JpaRepository<Subcause, Long> {

    /**
     * Return the all sub causes where are enabled.
     *
     * @param enable equal true.
     * @return the db sub causes info or null order by description ascending
     * @throws java.lang.Exception
     */
    public List<Subcause> findByEnableOrderByDescriptionAsc(boolean enable) throws Exception;

    /**
     * Return the sub causes having the passed causes.
     *
     * @param cause the causes.
     * @param enable equal true.
     * @return the db sub causes info or null of specific causes order by description ascending
     * @throws java.lang.Exception
     */
    public List findByCauseInAndEnableOrderByDescriptionAsc(List cause, Boolean enable) throws Exception;

    /**
     * Return the sub causes having the passed cause.
     *
     * @param cause the cause of damage.
     * @param enable equal true.
     * @return the db sub causes info or null of specific cause of damage order by description ascending
     * @throws java.lang.Exception
     */
    public List findByCauseAndEnableOrderByDescriptionAsc(Long cause, Boolean enable) throws Exception;
} // class SubcauseDao
