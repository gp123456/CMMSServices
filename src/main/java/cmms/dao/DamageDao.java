package cmms.dao;

import cmms.models.Damage;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * A DAO for the entity User is simply created by extending the CrudRepository
 * interface provided by spring. The following methods are some of the ones
 * available from such interface: save, delete, deleteAll, findOne and findAll.
 * The magic is that such methods must not be implemented, and moreover it is
 * possible create new query methods working only by defining their signature!
 *
 * @author netgloo
 */
@Repository
public interface DamageDao extends JpaRepository<Damage, Long>, JpaSpecificationExecutor<Damage> {

    /**
     * Return the damages having the passed department.
     *
     * @param from
     * @param to
     * @param department
     *            the department of plant.
     * @param deleted
     *            the delete parameter is false
     * @return the db damages info or null of specific department
     * @throws java.lang.Exception
     */
    public List findByCreatedBetweenAndDepartmentInAndDeletedOrderByCreatedDesc(Date from, Date to, List department,
	    Boolean deleted) throws Exception;

    /**
     * Return the damages having the passed department.
     *
     * @param from
     * @param to
     * @param department
     *            the department of plant.
     * @param deleted
     *            the delete parameter is false
     * @return the db damages info or null of specific department
     * @throws java.lang.Exception
     */
    public List findByInsertedBetweenAndDepartmentAndDeletedOrderByCreatedDesc(Date from, Date to, Long department,
	    Boolean deleted) throws Exception;

    /**
     * Return the damages having the passed department and type.
     *
     * @param from
     * @param to
     * @param department
     *            the department of plant.
     * @param type
     *            the type of damage
     * @param deleted
     *            the delete parameter is false
     * @return the db damages info or null of specific department
     * @throws java.lang.Exception
     */
    public List findByInsertedBetweenAndDepartmentInAndTypeInAndDeletedOrderByCreatedDesc(Date from, Date to,
	    List department, List type, Boolean deleted) throws Exception;

    /**
     * Return the duration of damages with type is delay.
     *
     * @param from
     * @param to
     * @param department
     *            the list of departments of plant.
     * @param type
     *            the type parameter is delay
     * @param deleted
     *            the delete parameter is false
     * @return the db damages info or null of specific department
     * @throws java.lang.Exception
     */
    public List findByInsertedBetweenAndDepartmentInAndTypeAndDeletedOrderByCreatedDesc(Date from, Date to,
	    List department, Long type, Boolean deleted) throws Exception;

    /**
     * Return the damages having the passed department and type.
     *
     * @param from
     * @param to
     * @param department
     *            the department of plant.
     * @param type
     *            the type of damage
     * @param deleted
     *            the delete parameter is false
     * @return the db damages info or null of specific department
     * @throws java.lang.Exception
     */
    public List findByInsertedBetweenAndDepartmentAndTypeAndDeletedOrderByCreatedDesc(Date from, Date to,
	    Long department, Long type, Boolean deleted) throws Exception;

    /**
     * Return the damages having the passed machine.
     *
     * @param from
     * @param to
     * @param machine
     *            the machine of plant.
     * @param deleted
     *            the delete parameter is false
     * @return the db damages info or null of specific machine
     * @throws java.lang.Exception
     */
    public List<Damage> findByInsertedBetweenAndMachineAndDeletedOrderByCreatedDesc(Date from, Date to, Long machine,
	    Boolean deleted) throws Exception;

    /**
     * Return the damages having the passed machine and type.
     *
     * @param from
     * @param to
     * @param machine
     *            the machine of plant.
     * @param type
     *            the type of cause
     * @param deleted
     *            the delete parameter is false
     * @return the db damages info or null of specific machine
     * @throws java.lang.Exception
     */
    public List<Damage> findByInsertedBetweenAndMachineAndTypeAndDeletedOrderByCreatedDesc(Date from, Date to,
	    Long machine, Long type, Boolean deleted) throws Exception;

    /**
     * Return the deleted damages having the passed departments of user.
     *
     * @param department
     *            the list of departments of specific user
     * @param deleted
     *            the delete parameter is false
     * @return the db damages info or null of specific machine
     * @throws java.lang.Exception
     */
    public List findByDepartmentInAndDeletedOrderByCreatedDesc(List department, Boolean deleted) throws Exception;

    /**
     * Return the count of damages having the passed machine, type.
     *
     * @param from
     * @param to
     * @param machine
     *            the machine of plant.
     * @param type
     *            the type of cause.
     * @param deleted
     *            the delete parameter is false
     * @return the db damages info or null of specific machine and type
     * @throws java.lang.Exception
     */
    public Long countByInsertedBetweenAndMachineAndTypeAndDeleted(Date from, Date to, Long machine, Long type,
	    Boolean deleted) throws Exception;

    /**
     * Return the count of damages having the passed machine, type.
     *
     * @param from
     * @param to
     * @param machine
     *            the machine of plant.
     * @param type
     *            the type of cause.
     * @param deleted
     *            the delete parameter is false
     * @return the db damages info or null of specific machine and type
     * @throws java.lang.Exception
     */
    public Long countByInsertedBetweenAndMachineAndTypeInAndDeleted(Date from, Date to, Long machine, List type,
	    Boolean deleted) throws Exception;

    /**
     * Return the count of damages having the passed machines, type.
     *
     * @param from
     * @param to
     * @param machine
     *            the list of machines of plant.
     * @param type
     *            the type of cause.
     * @param deleted
     *            the delete parameter is false
     * @return the db damages info or null of specific machine and type
     * @throws java.lang.Exception
     */
    public Long countByInsertedBetweenAndMachineInAndTypeAndDeleted(Date from, Date to, List machine, Long type,
	    Boolean deleted) throws Exception;

    /**
     * Return the count of damages having the passed machines, type.
     *
     * @param from
     * @param to
     * @param department
     *            the list of machines of plant.
     * @param type
     *            the type of cause.
     * @param deleted
     *            the delete parameter is false
     * @return the db damages info or null of specific machine and type
     * @throws java.lang.Exception
     */
    public Long countByInsertedBetweenAndDepartmentInAndTypeInAndDeleted(Date from, Date to, List department, List type,
	    Boolean deleted) throws Exception;

    /**
     * Return the count of damages having the passed department.
     *
     * @param department
     *            the department of plant.
     * @param deleted
     *            the delete parameter is false
     * @return the db damages info or null of specific machine and type
     * @throws java.lang.Exception
     */
    public Long countByDepartmentAndDeleted(Long department, Boolean deleted) throws Exception;

    /**
     * Return the sum duration by filter and group by cause.
     *
     * @param department
     *            the department of plant.
     * @param start
     *            the start date of shift.
     * @param type
     * @param end
     *            the end date of shift.
     * @return the sum duration of damages in specific machine
     * @throws java.lang.Exception
     */
    @Query("SELECT d.cause, SUM(d.duration), d.machine FROM Damage d WHERE d.deleted = 0 AND d.department in :department AND d.type != :type AND "
	    + "d.inserted BETWEEN :start AND :end GROUP BY d.cause")
    public List<Object[]> sumDurationByDepartmentInNoTypeShift(@Param("department") List department,
	    @Param("type") Long type, @Param("start") Date start, @Param("end") Date end) throws Exception;

    /**
     * Return the sum duration by filter and group by cause.
     *
     * @param department
     *            the department of plant.
     * @param start
     *            the start date of shift.
     * @param type
     * @param end
     *            the end date of shift.
     * @return the sum duration of damages in specific machine
     * @throws java.lang.Exception
     */
    @Query("SELECT d.cause, SUM(d.duration), d.machine FROM Damage d WHERE d.deleted = 0 AND d.department in :department AND d.type = :type AND "
	    + "d.inserted BETWEEN :start AND :end GROUP BY d.cause")
    public List<Object[]> sumDurationByDepartmentInTypeShift(@Param("department") List department,
	    @Param("type") Long type, @Param("start") Date start, @Param("end") Date end) throws Exception;

    /**
     * Return the sum duration by filter.
     *
     * @param department
     *            the department of plant.
     * @param start
     *            the start date of shift.
     * @param end
     *            the end date of shift.
     * @return the sum duration of damages in specific machine
     * @throws java.lang.Exception
     */
    @Query("SELECT SUM(d.duration) FROM Damage d WHERE d.deleted = 0 AND d.department in :department AND d.inserted BETWEEN :start AND :end")
    public Long sumDurationByDepartmentInShift(@Param("department") List department, @Param("start") Date start,
	    @Param("end") Date end) throws Exception;

    /**
     * Return the count machines by departments and current shift.
     *
     * @param department
     *            the department of plant.
     * @param start
     *            the start date of shift.
     * @param end
     *            the end date of shift.
     * @return the sum duration of damages in specific machine
     * @throws java.lang.Exception
     */
    @Query("SELECT count(d) FROM Damage d WHERE d.deleted = 0 AND d.department in :department AND d.created BETWEEN :start AND :end GROUP BY d.machine")
    public List countMachinesByDepartmentInShift(@Param("department") List department, @Param("start") Date start,
	    @Param("end") Date end) throws Exception;

    /**
     * Return the sum duration by filter and group by cause.
     *
     * @param machine
     *            the machine of specific department of plant.
     * @param type
     * @param start
     *            the start date of shift.
     * @param end
     *            the end date of shift.
     * @return the sum duration of damages in specific machine
     * @throws java.lang.Exception
     */
    @Query("SELECT d.cause, SUM(d.duration) FROM Damage d WHERE d.deleted = 0 AND d.machine = :machine AND d.type != :type AND d.inserted BETWEEN :start "
	    + "AND :end GROUP BY d.cause")
    public List<Object[]> sumDurationByMachineNoTypeShift(@Param("machine") Long machine, @Param("type") Long type,
	    @Param("start") Date start, @Param("end") Date end) throws Exception;

    /**
     * Return the sum duration by filter and group by cause.
     *
     * @param machine
     *            the machine of specific department of plant.
     * @param type
     * @param start
     *            the start date of shift.
     * @param end
     *            the end date of shift.
     * @return the sum duration of damages in specific machine
     * @throws java.lang.Exception
     */
    @Query("SELECT d.cause, SUM(d.duration) FROM Damage d WHERE d.deleted = 0 AND d.machine = :machine AND d.type = :type AND d.inserted BETWEEN :start "
	    + "AND :end GROUP BY d.cause")
    public List<Object[]> sumDurationByMachineTypeShift(@Param("machine") Long machine, @Param("type") Long type,
	    @Param("start") Date start, @Param("end") Date end) throws Exception;

    /**
     * Return the sum duration by filter.
     *
     * @param machine
     *            the machine of specific department of plant.
     * @param start
     *            the start date of shift.
     * @param end
     *            the end date of shift.
     * @return the sum duration of damages in specific machine
     * @throws java.lang.Exception
     */
    @Query("SELECT SUM(d.duration) FROM Damage d WHERE d.deleted = 0 AND d.machine = :machine AND d.inserted BETWEEN :start AND :end")
    public Long sumDurationByMachineShift(@Param("machine") Long machine, @Param("start") Date start,
	    @Param("end") Date end) throws Exception;

    /**
     * Return the count causes by following filter.
     *
     * @param department
     *            the department of plant.
     * @param start
     *            the start date of shift.
     * @param end
     *            the end date of shift.
     * @return the sum duration of damages in specific machine
     * @throws java.lang.Exception
     */
    @Query("SELECT COUNT(*) FROM Damage d WHERE d.deleted = 0 AND d.department = :department AND d.type != 3 AND d.created BETWEEN :start AND :end")
    public Long countCauseByDepartmentShift(@Param("department") Long department, @Param("start") Date start,
	    @Param("end") Date end) throws Exception;
} // class DamageDao
