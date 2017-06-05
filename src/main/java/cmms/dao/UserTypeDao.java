/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.dao;

import cmms.models.UserType;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author gpatitakis
 */
@Transactional
public interface UserTypeDao extends JpaRepository<UserType, Long> {

}
