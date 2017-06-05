/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.dao;

import cmms.models.CauseType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author gpatitakis
 */
public interface CauseTypeDao extends JpaRepository<CauseType, Long> {
    Collection<CauseType> findByIdInOrderByNameAsc(List id);

} // class CauseTypeDao
