/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.controllers;

import cmms.dao.CauseDao;
import cmms.dao.CauseTypeDao;
import cmms.dao.DamageDao;
import static cmms.dao.DamageSpecs.between;
import static cmms.dao.DamageSpecs.departments;
import static cmms.dao.DamageSpecs.machines;
import static cmms.dao.DamageSpecs.types;
import static cmms.dao.DamageSpecs.causes;
import static cmms.dao.DamageSpecs.deleted;
import static cmms.dao.DamageSpecs.users;
import cmms.dao.DelayDao;
import cmms.dao.DepartmentDao;
import cmms.dao.MachineDao;
import cmms.dao.SubcauseDao;
import cmms.dao.UserDao;
import cmms.dao.UserDepartmentDao;
import cmms.enums.CauseTypeEnum;
import cmms.enums.UserTypeEnum;
import cmms.helpers.Shift;
import cmms.models.Cause;
import cmms.models.CauseType;
import cmms.models.Criteria;
import cmms.models.Damage;
import cmms.models.Delay;
import cmms.models.Department;
import cmms.models.DepartmentPareto;
import cmms.models.Machine;
import cmms.models.Pareto;
import cmms.models.Subcause;
import cmms.models.User;
import cmms.models.UserDepartment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author gpatitakis
 */
@Controller
public class DamagesController {

    private static final Logger logger = Logger.getLogger(DamagesController.class.getName());

    private static final class CriteriaType {

	private static final Integer PARETO = 1;
	private static final Integer DAMAGE = 2;
    }

    private Date paretoFrom;

    private Date paretoTo;

    private Criteria criteria;

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    /**
     * all --> Return all damage by user department(s) of db in JSON format.
     *
     * @param id
     *            the user id
     * @return the db damage info or error message.
     */
    @RequestMapping("/damage/user")
    @ResponseBody
    @SuppressWarnings("null")
    public String user(String id) {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Date from = null;
	Map<String, String> values = new HashMap<>();
	String response = null;

	try {
	    from = sdf.parse(Shift.getStart(new Date()));
	} catch (ParseException ex) {
	    Logger.getLogger(DamagesController.class.getName()).log(Level.SEVERE, null, ex);
	}
	logger.log(Level.INFO, "Get all damages for all department(s) of user id:{0} and current shift:[{1},{2}] ",
		new Object[] { id, from.toString(), new Date().toString() });
	try {
	    if (id != null) {
		User user = userDao.findOne(id);

		if (user != null) {
		    ObjectMapper mapper = new ObjectMapper();
		    ObjectWriter typedWriter = mapper.writerWithType(
			    mapper.getTypeFactory().constructCollectionType(Collection.class, Damage.class));
		    Collection<UserDepartment> uds = userDepartmentDao.findByUsers(id);

		    if (uds != null && !uds.isEmpty()) {
			List<Long> departments = new ArrayList<>();

			uds.stream().forEach((ud) -> {
			    departments.add(ud.getDepartment());
			});

			Collection<Damage> damages = damageDao
				.findByCreatedBetweenAndDepartmentInAndTypeInAndDeletedOrderByCreatedDesc(from,
					new Date(), departments,
					(user.getType().equals(UserTypeEnum.ELECTRICIAN.getId()))
						? Arrays.asList(CauseTypeEnum.ELECRTICAL.getId())
						: (user.getType().equals(UserTypeEnum.ENGINEER.getId()))
							? Arrays.asList(CauseTypeEnum.MECHANICAL.getId())
							: Arrays.asList(CauseTypeEnum.ELECRTICAL.getId(),
								CauseTypeEnum.MECHANICAL.getId(),
								CauseTypeEnum.DELAY.getId()),
					Boolean.FALSE);
			Double delayDuration = getDelayDurationCurrentShift(from, new Date(), departments, null) / 60.;
			Long period = new Date().getTime() - from.getTime();

			if (damages != null && !damages.isEmpty()) {
			    damages.stream().forEach((damage) -> {
				setDamageInfo(damage);
			    });
			    response = typedWriter.writeValueAsString(damages);
			    values.put("damages", response);
			    values.put("period", period.toString());
			    values.put("delayDuration", delayDuration.toString());
			    response = mapper.writeValueAsString(values);
			} else {
			    logger.log(Level.INFO, "There weren't damages for the user department(s) id:{0}", id);
			}
		    }
		}
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
	}

	return response;
    }

    /**
     * all --> Return all damage by machine of db in JSON format.
     *
     * @param id
     *            the id of machine
     * @param user
     * @return the db damage info or error message.
     */
    @RequestMapping("/damage/machine")
    @ResponseBody
    @SuppressWarnings("null")
    public String machine(Long id, String user) {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Date from = null;
	String response = null;
	Map<String, String> values = new HashMap<>();

	try {
	    from = sdf.parse(Shift.getStart(new Date()));
	} catch (ParseException ex) {
	    logger.log(Level.SEVERE, null, ex);
	}
	logger.log(Level.INFO, "Get all damages for machine id:{0} and current shift:[{1},{2}] and user:{3} ",
		new Object[] { id, from.toString(), new Date().toString(), user });

	try {
	    if (id != null && !Strings.isNullOrEmpty(user) && from != null) {
		User dbuser = userDao.findOne(user);

		if (dbuser != null) {
		    ObjectMapper mapper = new ObjectMapper();
		    ObjectWriter typedWriter = mapper.writerWithType(
			    mapper.getTypeFactory().constructCollectionType(Collection.class, Damage.class));

		    Collection<Damage> damages = (dbuser.getType().equals(UserTypeEnum.ELECTRICIAN.getId()))
			    ? damageDao.findByCreatedBetweenAndMachineAndTypeAndDeletedOrderByCreatedDesc(from,
				    new Date(), id, CauseTypeEnum.ELECRTICAL.getId(), Boolean.FALSE)
			    : (dbuser.getType().equals(UserTypeEnum.ENGINEER.getId()))
				    ? damageDao.findByCreatedBetweenAndMachineAndTypeAndDeletedOrderByCreatedDesc(from,
					    new Date(), id, CauseTypeEnum.MECHANICAL.getId(), Boolean.FALSE)
				    : damageDao.findByCreatedBetweenAndMachineAndDeletedOrderByCreatedDesc(from,
					    new Date(), id, Boolean.FALSE);
		    Double delayDuration = getDelayDurationCurrentShift(from, new Date(), null, id) / 60.;
		    Long period = new Date().getTime() - from.getTime();

		    if (damages != null && !damages.isEmpty()) {
			damages.stream().forEach((Damage damage) -> {
			    setDamageInfo(damage);
			});
			response = typedWriter.writeValueAsString(damages);
			values.put("damages", response);
			values.put("period", period.toString());
			values.put("delayDuration", delayDuration.toString());
			response = mapper.writeValueAsString(values);
		    } else {
			logger.log(Level.INFO, "There weren't damages for the department:{0} and user{1}",
				new Object[] { id, user });
		    }
		}
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "{0}", ex);
	}

	return response;
    }

    /**
     * all --> Return all damage by department of db in JSON format.
     *
     * @param id
     *            the id of department
     * @param user
     * @return the db damage info or error message.
     */
    @RequestMapping("/damage/department")
    @ResponseBody
    @SuppressWarnings("null")
    public String department(Long id, String user) {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Date from = null;
	Map<String, String> values = new HashMap<>();
	String response = null;

	try {
	    from = sdf.parse(Shift.getStart(new Date()));
	} catch (ParseException ex) {
	    logger.log(Level.SEVERE, null, ex);
	}
	logger.log(Level.INFO, "Get all damages for department id:{0} and current shift:[{1},{2}] and user:{3}",
		new Object[] { id, from.toString(), new Date().toString(), user });

	try {
	    if (id != null && !Strings.isNullOrEmpty(user) && from != null) {
		User dbuser = userDao.findOne(user);

		if (dbuser != null) {
		    ObjectMapper mapper = new ObjectMapper();
		    ObjectWriter typedWriter = mapper.writerWithType(
			    mapper.getTypeFactory().constructCollectionType(Collection.class, Damage.class));
		    Collection<Damage> damages = (dbuser.getType().equals(UserTypeEnum.ELECTRICIAN.getId()))
			    ? damageDao.findByCreatedBetweenAndDepartmentAndTypeAndDeletedOrderByCreatedDesc(from,
				    new Date(), id, CauseTypeEnum.ELECRTICAL.getId(), Boolean.FALSE)
			    : (dbuser.getType().equals(UserTypeEnum.ENGINEER.getId()))
				    ? damageDao.findByCreatedBetweenAndDepartmentAndTypeAndDeletedOrderByCreatedDesc(
					    from, new Date(), id, CauseTypeEnum.MECHANICAL.getId(), Boolean.FALSE)
				    : damageDao.findByCreatedBetweenAndDepartmentAndDeletedOrderByCreatedDesc(from,
					    new Date(), id, Boolean.FALSE);
		    Double delayDuration = getDelayDurationCurrentShift(from, new Date(), Arrays.asList(id), null)
			    / 60.;
		    Long period = new Date().getTime() - from.getTime();

		    if (damages != null && !damages.isEmpty()) {
			damages.stream().forEach((Damage damage) -> {
			    setDamageInfo(damage);
			});
			response = typedWriter.writeValueAsString(damages);
			values.put("damages", response);
			values.put("period", period.toString());
			values.put("delayDuration", delayDuration.toString());
			response = mapper.writeValueAsString(values);
		    } else {
			logger.log(Level.INFO, "There weren't damages for the department:{0}", id);
		    }
		}
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "{0}", ex);
	}

	return response;
    }

    /**
     * list by criteria --> Return damages by criteria in JSON format.
     *
     * @return the db damage info or error message.
     */
    @RequestMapping("/damage/filter")
    @ResponseBody
    @SuppressWarnings("null")
    public String filterDamage() {
	String response = null;
	Map<String, String> values = new HashMap<>();

	try {
	    List<Damage> damages = getDamageSpecific(CriteriaType.DAMAGE, "created");
	    Double delayDuration = getDelaySpecific();
	    ObjectMapper mapper = new ObjectMapper();

	    if (damages != null && !damages.isEmpty()) {
		damages.stream().forEach((damage) -> {
		    setDamageInfo(damage);
		});
		ObjectWriter typedWriter = mapper.writerWithType(
			mapper.getTypeFactory().constructCollectionType(Collection.class, Damage.class));
		Long period = criteria.getTo().getTime() - criteria.getFrom().getTime();

		response = typedWriter.writeValueAsString(damages);
		values.put("damages", response);
		values.put("period", period.toString());
		values.put("delayDuration", (delayDuration != null) ? delayDuration.toString() : "0.0");
		response = mapper.writeValueAsString(values);
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
	}

	return response;
    }

    /**
     * one --> Return a damage by id of db in JSON format.
     *
     * @param id
     * @return the db cause types info or error message.
     */
    @RequestMapping("/damage")
    @ResponseBody
    public String damage(Long id) {
	logger.log(Level.INFO, "Get a damage with id:{0}", id);

	String response = null;

	try {
	    if (id != null) {
		ObjectMapper mapper = new ObjectMapper();
		Damage damage = damageDao.findOne(id);

		if (damage != null) {
		    setDamageInfo(damage);
		    response = mapper.writeValueAsString(damage);
		} else {
		    logger.log(Level.INFO, "no found damage with id{0}", id);
		}
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
	}

	return response;
    }

    /**
     * more --> Return the deleted damage(s) by user id of db in JSON format.
     *
     * @param userId
     * @return the db damages info or error message.
     */
    @RequestMapping("/damage/deleted")
    @ResponseBody
    public String deletedDamages(String userId) {
	logger.log(Level.INFO, "Get the damage(s) with user id:{0}", userId);

	String response = null;
	Map<String, String> values = new HashMap<>();

	try {
	    if (!Strings.isNullOrEmpty(userId)) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter typedWriter = mapper.writerWithType(
			mapper.getTypeFactory().constructCollectionType(Collection.class, Damage.class));
		List<UserDepartment> uds = userDepartmentDao.findByUsers(userId);

		if (uds != null && !uds.isEmpty()) {
		    List<Long> departmentIds = new ArrayList<>();

		    uds.stream().forEach((ud) -> {
			departmentIds.add(ud.getDepartment());
		    });
		    List<Damage> damages = damageDao.findByDepartmentInAndDeletedOrderByCreatedDesc(departmentIds,
			    Boolean.TRUE);

		    if (damages != null && !damages.isEmpty()) {
			damages.stream().forEach((damage) -> {
			    setDamageInfo(damage);
			});
			response = typedWriter.writeValueAsString(damages);
			values.put("damages", response);
			response = mapper.writeValueAsString(values);
		    } else {
			logger.log(Level.INFO, "There weren't damages for the user department id(s):{0}",
				Arrays.toString(departmentIds.toArray(new Long[departmentIds.size()])));
		    }
		} else {
		    logger.log(Level.INFO, "no found damage with user id{0}", userId);
		}
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
	}

	return response;
    }

    /**
     * list by criteria --> Return damages by criteria in JSON format.
     *
     * @return the db damage info or error message.
     */
    @RequestMapping("/damage/pareto/filter")
    @ResponseBody
    @SuppressWarnings("null")
    public String filterPareto() {
	String response = null;

	try {
	    List<Damage> damages = getDamageSpecific(CriteriaType.PARETO, "cause");

	    if (damages != null && !damages.isEmpty()) {
		Long machines = getMachinesByDamages(damages);
		Double criteriaDuration = ((paretoTo.getTime() - paretoFrom.getTime()) * machines) / 60000.;
		Double delayDuration = getDelaySpecific();
		Double totalDuration = getTotalDuration(damages);
		ObjectMapper mapper = new ObjectMapper();
		Map<Cause,Long> level1Cause= new HashMap<>();
		List<Pareto> paretos = new ArrayList<>();
		Long totalCause = getTotalCause(damages);
		Long causeId = 0l, departmentId = 0l, typeId = 0l, sum = 0l, causeSum= 0l;

		Collections.sort(damages, (a, b) -> b.getCause().compareTo(a.getCause()));

		for (Damage damage : damages) {
		    if (!causeId.equals(damage.getCause())) {
			if (!causeId.equals(0l)) {
			    if (damage.getType().equals(CauseTypeEnum.DELAY.getId())) {
				Delay delay = delayDao.findByIdAndDepartment(causeId, departmentId);

				if (delay != null) {
				    paretos.add(new Pareto(delay.getDescription(), sum));
				}
			    } else {
				Subcause subcause = subcauseDao.findOne(causeId);

				if (subcause != null) {
				    if (subcause.getCause() != null) {
					Cause cause = causeDao.findOne(subcause.getCause());

					if (level1Cause.isEmpty() || !level1Cause.containsKey(cause)){
                                        causeSum = sum;
                                        level1Cause.put(cause, causeSum);
                                        } else{
                                            if (level1Cause.containsKey(cause)){
                                                causeSum = level1Cause.get(cause).longValue();
                                                causeSum += sum;
                                                level1Cause.put(cause, causeSum);
                                            }
                                        }
                                        if (criteria.getCauses().length != 0){
                                            paretos.add(new Pareto(cause.getDescription() + "[" + subcause.getDescription() + "]", sum));
                                        }
				    } else {
					paretos.add(new Pareto(subcause.getDescription(), sum));
				    }
				}
			    }
			}
			causeId = damage.getCause();
                        departmentId = damage.getDepartment();
                        typeId = damage.getType();
			sum = damage.getDuration();
		    } else {
			sum += damage.getDuration();
		    }
		}
                if (typeId.equals(CauseTypeEnum.DELAY.getId())) {
                    Delay delay = delayDao.findByIdAndDepartment(causeId, departmentId);
                    if (delay != null) {
                        paretos.add(new Pareto(delay.getDescription(), sum));
                    }
                } else {
                    Subcause subcause = subcauseDao.findOne(causeId);

                    if (subcause != null) {
                        if (subcause.getCause() != null) {
                            Cause cause = causeDao.findOne(subcause.getCause());

                            if (level1Cause.isEmpty() || !level1Cause.containsKey(cause)){
                                causeSum = sum;
                                level1Cause.put(cause, causeSum);
                            } else{
                                if (level1Cause.containsKey(cause)){
                                    causeSum = level1Cause.get(cause).longValue();
                                    causeSum += sum;
                                    level1Cause.put(cause, causeSum);
                                }
                            }
                            if (criteria.getCauses().length != 0){
                                paretos.add(new Pareto(cause.getDescription() + "[" + subcause.getDescription() + "]", sum));
                            }
                        } else {
                            paretos.add(new Pareto(subcause.getDescription(), sum));
                        }
                    }
                }
		
		if (criteria.getCauses().length == 0){
                   for (Map.Entry<Cause, Long> entry : level1Cause.entrySet()){
                       paretos.add(new Pareto(entry.getKey().getDescription(), entry.getValue()));
                   }
               }

		Collections.sort(paretos, (a, b) -> b.getDelay().compareTo(a.getDelay()));

		for (int i = 0; i < paretos.size(); i++) {
		    Double current = paretos.get(i).getDelay();
		    Double prev = 0.0;

		    if (i > 0) {
			for (int j = 0; j < i; j++) {
			    prev += paretos.get(j).getDelay();
			}
		    } else {
			prev = null;
		    }

		    paretos.get(i).setPercent(getPercent(current, prev, totalDuration / 60.0));
		}

		response = mapper.writeValueAsString(new DepartmentPareto(1l, "current filter", paretos,
			new BigDecimal(totalDuration / totalCause).setScale(2, RoundingMode.CEILING),
			new BigDecimal((criteriaDuration - (totalDuration + delayDuration)) / totalCause).setScale(2,
				RoundingMode.CEILING),
			getMachineCodes(damages)));
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, null, ex);
	}

	return response;
    }

    /**
     * list --> Return a list of summaries of damages by department of specific
     * user in JSON format.
     *
     * @param user
     * @return the db cause types info or error message.
     */
    @RequestMapping("/damage/pareto/department")
    @ResponseBody
    @SuppressWarnings("null")
    public String paretoDepartment(String user) {
	logger.log(Level.INFO, "Get pareto of damages per department with user id:{0}", user);

	String response = null;

	try {
	    if (!Strings.isNullOrEmpty(user)) {
		ObjectMapper mapper = new ObjectMapper();
		List<UserDepartment> uds = userDepartmentDao.findByUsers(user);
		User dbuser = userDao.findOne(user);

		if (uds != null && !uds.isEmpty() && user != null) {
		    try {
			List<Long> departmentIds = new ArrayList<>();
			uds.stream().forEach((ud) -> {
			    departmentIds.add(ud.getDepartment());
			});
			List<Pareto> paretos = new ArrayList<>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date from = sdf.parse(Shift.getStart(new Date()));
			List<Object[]> sumDurationCauses = (dbuser.getType().equals(UserTypeEnum.ELECTRICIAN.getId()))
				? damageDao.sumDurationByDepartmentInTypeShift(departmentIds,
					CauseTypeEnum.ELECRTICAL.getId(), from, new Date())
				: (dbuser.getType().equals(UserTypeEnum.ENGINEER.getId()))
					? damageDao.sumDurationByDepartmentInTypeShift(departmentIds,
						CauseTypeEnum.MECHANICAL.getId(), from, new Date())
					: damageDao.sumDurationByDepartmentInNoTypeShift(departmentIds,
						CauseTypeEnum.DELAY.getId(), from, new Date());
			Double totalDuration = damageDao.sumDurationByDepartmentInShift(departmentIds, from, new Date())
				/ 60.0;
			Long totalCause = (dbuser.getType()
				.equals(UserTypeEnum.ELECTRICIAN.getId()))
					? damageDao.countByCreatedBetweenAndDepartmentInAndTypeInAndDeleted(
						from, new Date(),
						departmentIds, Arrays.asList(CauseTypeEnum.ELECRTICAL.getId()),
						Boolean.FALSE)
					: (dbuser.getType().equals(UserTypeEnum.ENGINEER.getId()))
						? damageDao.countByCreatedBetweenAndDepartmentInAndTypeInAndDeleted(
							from, new Date(), departmentIds,
							Arrays.asList(CauseTypeEnum.MECHANICAL.getId()), Boolean.FALSE)
						: damageDao
							.countByCreatedBetweenAndDepartmentInAndTypeInAndDeleted(from,
								new Date(), departmentIds,
								Arrays.asList(CauseTypeEnum.ELECRTICAL.getId(),
									CauseTypeEnum.MECHANICAL.getId()),
								Boolean.FALSE);
			Long causeDuration = 0l;
			@SuppressWarnings("UnusedAssignment")
			List<Damage> damages = null;

			if (sumDurationCauses != null && !sumDurationCauses.isEmpty()) {
			    damages = new ArrayList<>();

			    for (Object[] values : sumDurationCauses) {
				Subcause subcause = subcauseDao.findOne((Long) values[0]);

				damages.add(new Damage.Builder().setMachine((Long) values[2]).build());
				if (subcause != null) {
				    if (subcause.getCause() != null) {
					Cause cause = causeDao.findOne(subcause.getCause());

					paretos.add(new Pareto(
						cause.getDescription() + "[" + subcause.getDescription() + "]",
						(Long) values[1]));
				    } else {
					paretos.add(new Pareto(subcause.getDescription(), (Long) values[1]));
				    }
				    causeDuration += (Long) values[1];
				}
			    }

			    Collections.sort(paretos, (a, b) -> b.getDelay().compareTo(a.getDelay()));

			    for (int i = 0; i < paretos.size(); i++) {
				Double current = paretos.get(i).getDelay();
				Double prev = 0.0;

				if (i > 0) {
				    for (int j = 0; j < i; j++) {
					prev += paretos.get(j).getDelay();
				    }
				} else {
				    prev = null;
				}

				paretos.get(i).setPercent(getPercent(current, prev, causeDuration / 3600.));
			    }

			    Double currentShiftDuration = (new Date().getTime() - from.getTime()) / 60000.;
			    List<Long> dbmachines = damageDao.countMachinesByDepartmentInShift(departmentIds, from,
				    new Date());
			    Long machines = (dbmachines != null && !dbmachines.isEmpty()) ? dbmachines.size() : 1l;

			    response = mapper.writeValueAsString(new DepartmentPareto(
				    Long.valueOf(departmentIds.size()), "login user:" + user, paretos,
				    new BigDecimal((causeDuration / 60.) / totalCause).setScale(2,
					    RoundingMode.CEILING),
				    new BigDecimal(((currentShiftDuration * machines) - totalDuration) / totalCause)
					    .setScale(2, RoundingMode.CEILING),
				    (damages != null) ? getMachineCodes(damages) : ""));
			}
		    } catch (Exception ex) {
			logger.log(Level.SEVERE, null, ex);
		    }
		}
	    } else {
		logger.log(Level.INFO, "no found damage with id{0}", user);
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
	}

	return response;
    }

    /**
     * list --> Return a list of summaries of damages by department id in JSON
     * format.
     *
     * @param id
     * @param user
     * @return the db cause types info or error message.
     */
    @RequestMapping("/damage/pareto/departmentId")
    @ResponseBody
    @SuppressWarnings("null")
    public String paretoDepartmentId(Long id, String user) {
	logger.log(Level.INFO, "Get pareto of damages per department with id:{0} and user{0}",
		new Object[] { id, user });

	String response = null;

	try {
	    if (id != null && !Strings.isNullOrEmpty(user)) {
		User dbuser = userDao.findOne(user);

		if (dbuser != null) {
		    ObjectMapper mapper = new ObjectMapper();

		    try {
			List<Pareto> paretos = new ArrayList<>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date from = sdf.parse(Shift.getStart(new Date()));
			List<Object[]> sumDurationCauses = (dbuser.getType().equals(UserTypeEnum.ELECTRICIAN.getId()))
				? damageDao.sumDurationByDepartmentInTypeShift(Arrays.asList(id),
					CauseTypeEnum.ELECRTICAL.getId(), from, new Date())
				: (dbuser.getType().equals(UserTypeEnum.ENGINEER.getId()))
					? damageDao.sumDurationByDepartmentInTypeShift(Arrays.asList(id),
						CauseTypeEnum.MECHANICAL.getId(), from, new Date())
					: damageDao.sumDurationByDepartmentInNoTypeShift(Arrays.asList(id),
						CauseTypeEnum.DELAY.getId(), from, new Date());
			Double totalDuration = damageDao.sumDurationByDepartmentInShift(Arrays.asList(id), from,
				new Date()) / 60.;
			Long totalCause = (dbuser.getType()
				.equals(UserTypeEnum.ELECTRICIAN.getId()))
					? damageDao.countByCreatedBetweenAndDepartmentInAndTypeInAndDeleted(
						from, new Date(),
						Arrays.asList(id), Arrays.asList(CauseTypeEnum.ELECRTICAL.getId()),
						Boolean.FALSE)
					: (dbuser.getType().equals(UserTypeEnum.ENGINEER.getId()))
						? damageDao.countByCreatedBetweenAndDepartmentInAndTypeInAndDeleted(
							from, new Date(), Arrays.asList(id),
							Arrays.asList(CauseTypeEnum.MECHANICAL.getId()), Boolean.FALSE)
						: damageDao
							.countByCreatedBetweenAndDepartmentInAndTypeInAndDeleted(from,
								new Date(), Arrays.asList(id),
								Arrays.asList(CauseTypeEnum.ELECRTICAL.getId(),
									CauseTypeEnum.MECHANICAL.getId()),
								Boolean.FALSE);
			Long causeDuration = 0l;
			@SuppressWarnings("UnusedAssignment")
			List<Damage> damages = null;

			if (sumDurationCauses != null && !sumDurationCauses.isEmpty()) {
			    damages = new ArrayList<>();

			    for (Object[] values : sumDurationCauses) {
				Subcause subcause = subcauseDao.findOne((Long) values[0]);

				damages.add(new Damage.Builder().setMachine((Long) values[2]).build());
				if (subcause != null) {
				    if (subcause.getCause() != null) {
					Cause cause = causeDao.findOne(subcause.getCause());

					paretos.add(new Pareto(
						cause.getDescription() + "[" + subcause.getDescription() + "]",
						(Long) values[1]));
				    } else {
					paretos.add(new Pareto(subcause.getDescription(), (Long) values[1]));
				    }
				    causeDuration += (Long) values[1];
				}
			    }

			    Collections.sort(paretos, (a, b) -> b.getDelay().compareTo(a.getDelay()));

			    for (int i = 0; i < paretos.size(); i++) {
				Double current = paretos.get(i).getDelay();
				Double prev = 0.0;

				if (i > 0) {
				    for (int j = 0; j < i; j++) {
					prev += paretos.get(j).getDelay();
				    }
				} else {
				    prev = null;
				}

				paretos.get(i).setPercent(getPercent(current, prev, causeDuration / 3600.));
			    }

			    Double currentShiftDuration = (new Date().getTime() - from.getTime()) / 60000.;
			    List<Long> dbmachines = damageDao.countMachinesByDepartmentInShift(Arrays.asList(id), from,
				    new Date());
			    Long machines = (dbmachines != null && !dbmachines.equals(0l)) ? dbmachines.size() : 1l;

			    response = mapper.writeValueAsString(new DepartmentPareto(id, "", paretos,
				    new BigDecimal((causeDuration / 60.) / totalCause).setScale(2,
					    RoundingMode.CEILING),
				    new BigDecimal(((currentShiftDuration * machines) - totalDuration) / totalCause)
					    .setScale(2, RoundingMode.CEILING),
				    (damages != null) ? getMachineCodes(damages) : ""));
			}
		    } catch (Exception ex) {
			logger.log(Level.SEVERE, null, ex);
		    }
		}
	    } else {
		logger.log(Level.INFO, "no found damage with department id{0} and useer:{1}",
			new Object[] { id, user });
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
	}

	return response;
    }

    /**
     * list --> Return pareto of damages of specific machine in JSON format.
     *
     * @param id
     * @param user
     * @return the db cause types info or error message.
     */
    @RequestMapping("/damage/pareto/machine")
    @ResponseBody
    public String paretoMachine(Long id, String user) {
	logger.log(Level.INFO, "Get pareto damages per machine with id:{0} and user{1}", new Object[] { id, user });

	String response = null;

	try {
	    if (id != null && !Strings.isNullOrEmpty(user)) {
		User dbuser = userDao.findOne(user);

		if (dbuser != null) {
		    ObjectMapper mapper = new ObjectMapper();
		    Machine machine = machineDao.findOne(id);

		    if (machine != null) {
			try {
			    List<Pareto> paretos = new ArrayList<>();
			    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    Date from = sdf.parse(Shift.getStart(new Date()));
			    List<Object[]> sumDurationCauses = (dbuser.getType()
				    .equals(UserTypeEnum.ELECTRICIAN.getId()))
					    ? damageDao.sumDurationByMachineTypeShift(machine.getId(),
						    CauseTypeEnum.ELECRTICAL.getId(), from, new Date())
					    : (dbuser.getType().equals(UserTypeEnum.ENGINEER.getId()))
						    ? damageDao.sumDurationByMachineTypeShift(machine.getId(),
							    CauseTypeEnum.MECHANICAL.getId(), from, new Date())
						    : damageDao.sumDurationByMachineNoTypeShift(machine.getId(),
							    CauseTypeEnum.DELAY.getId(), from, new Date());
			    Double totalDuration = damageDao.sumDurationByMachineShift(machine.getId(), from,
				    new Date()) / 60.;
			    Long totalCause = (dbuser.getType().equals(UserTypeEnum.ELECTRICIAN.getId()))
				    ? damageDao.countByCreatedBetweenAndMachineAndTypeInAndDeleted(from,
					    new Date(), id, Arrays.asList(CauseTypeEnum.ELECRTICAL.getId()),
					    Boolean.FALSE)
				    : (dbuser.getType().equals(UserTypeEnum.ENGINEER.getId()))
					    ? damageDao.countByCreatedBetweenAndMachineAndTypeInAndDeleted(
						    from, new Date(), id,
						    Arrays.asList(CauseTypeEnum.MECHANICAL.getId()), Boolean.FALSE)
					    : damageDao.countByCreatedBetweenAndMachineAndTypeInAndDeleted(from,
						    new Date(), id, Arrays.asList(CauseTypeEnum.ELECRTICAL.getId(),
							    CauseTypeEnum.MECHANICAL.getId()),
						    Boolean.FALSE);
			    Long causeDuration = 0l;

			    if (sumDurationCauses != null && !sumDurationCauses.isEmpty()) {
				for (Object[] values : sumDurationCauses) {
				    Subcause subcause = subcauseDao.findOne((Long) values[0]);

				    if (subcause != null) {
					if (subcause.getCause() != null) {
					    Cause cause = causeDao.findOne(subcause.getCause());

					    paretos.add(new Pareto(
						    cause.getDescription() + "[" + subcause.getDescription() + "]",
						    (Long) values[1]));
					} else {
					    paretos.add(new Pareto(subcause.getDescription(), (Long) values[1]));
					}
					causeDuration += (Long) values[1];
				    }
				}

				Collections.sort(paretos, (a, b) -> b.getDelay().compareTo(a.getDelay()));

				for (int i = 0; i < paretos.size(); i++) {
				    Double current = paretos.get(i).getDelay();
				    Double prev = 0.0;

				    if (i > 0) {
					for (int j = 0; j < i; j++) {
					    prev += paretos.get(j).getDelay();
					}
				    } else {
					prev = null;
				    }

				    paretos.get(i).setPercent(getPercent(current, prev, causeDuration / 3600.));
				}

				Double currentShiftDuration = (new Date().getTime() - from.getTime()) / 60000.;
				response = mapper.writeValueAsString(
					new DepartmentPareto(machine.getId(), machine.getCode(), paretos,
						new BigDecimal((causeDuration / 60.) / totalCause).setScale(2,
							RoundingMode.CEILING),
						new BigDecimal((currentShiftDuration - totalDuration) / totalCause)
							.setScale(2, RoundingMode.CEILING),
						machine.getCode()));
			    }
			} catch (Exception ex) {
			    logger.log(Level.SEVERE, null, ex);
			}
		    }
		}
	    } else {
		logger.log(Level.INFO, "no found damage with machine id{0}", id);
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
	}

	return response;
    }

    /**
     * update damage --> Return a success message for update damage.
     *
     * @return the success message or error message.
     */
    @RequestMapping("/damage/update")
    @ResponseBody
    public String update() {
	String response = null;

	try {
	    BufferedReader reader = request.getReader();
	    ObjectMapper mapper = new ObjectMapper();
	    String jsonDamage = "", line;
	    if (reader.ready()) {
		while ((line = reader.readLine()) != null) {
		    jsonDamage += line;
		}
		logger.log(Level.INFO, "Update damage with info:{0}", jsonDamage);

		if (!Strings.isNullOrEmpty(jsonDamage)) {
		    Damage damage = mapper.readValue(jsonDamage, Damage.class);

		    if (damage != null) {
			Damage d = damageDao.findOne(damage.getId());

			if (d != null) {
			    if (!Strings.isNullOrEmpty(damage.getUser())) {
				User user = userDao.findOne(damage.getUser());

				if (user != null) {
				    d.setUser(user.getId());
				}
			    }
			    if (!Strings.isNullOrEmpty(damage.getNote()) && !damage.getNote().equals(d.getNote())) {
				d.setNote(damage.getNote());
			    }
			    if (damage.getType() != null) {
				CauseType type = causeTypeDao.findOne(damage.getType());

				if (type != null) {
				    d.setType(type.getId());
				}
			    }
			    if (damage.getCause() != null) {
				if (damage.getType().equals(CauseTypeEnum.DELAY.getId())) {
				    Delay delay = delayDao.findByIdAndDepartment(damage.getCause(), damage.getDepartment());

				    if (delay != null) {
					d.setCause(delay.getId());
					d.setQ32$IDDLR(delay.getId().toString());
				    }
				} else {
				    Subcause subcause = subcauseDao.findOne(damage.getCause());

				    if (subcause != null) {
					d.setCause(subcause.getId());
					d.setQ32$IDDLR(subcause.getId().toString());
				    }
				}

			    }
			    if (damage.getDuration() != null) {
				d.setDuration(damage.getDuration() * 60);
			    }
			    d.setQ32$STA("3");

			    d = damageDao.saveAndFlush(d);

			    if (d != null) {
				response = "Success update damage with id:" + d.getId();
			    }
			}
		    }
		}
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
	}

	return response;
    }

    /**
     * insert damage --> Return a success message for insert new damage.
     *
     * @return the success message or error message.
     */
    @RequestMapping("/damage/insert")
    @ResponseBody
    @SuppressWarnings("null")
    public String insert() {
	String response = null;

	try {
	    BufferedReader reader = request.getReader();
	    ObjectMapper mapper = new ObjectMapper();
	    String jsonDamage = "", line;
	    if (reader.ready()) {
		while ((line = reader.readLine()) != null) {
		    jsonDamage += line;
		}
		logger.log(Level.INFO, "Insert damage with info:{0}", jsonDamage);

		if (!Strings.isNullOrEmpty(jsonDamage)) {
		    Damage damage = mapper.readValue(jsonDamage, Damage.class);

		    if (damage != null) {
			Machine machine = machineDao.findOne(damage.getMachine());
			Long iwukidp = machine.getIWUKIDP();

			damage.setCause(damage.getCause());
			if (machine != null) {
			    damage.setDepartment(machine.getDepartment());
			    damage.setQ32$FAAL1(machine.getIW$FAAL1());
			    damage.setQ32$IDDLR(damage.getCause().toString());
			    damage.setQ32$LINE(machine.getIW$LINE());
			    damage.setQ32$SHFT("3");
			    damage.setQ32$STA("0");
			    damage.setQ32$STEP(machine.getIW$STEP());
			    damage.setQ32LOCN(machine.getIWLOCN());
			    damage.setQ32MCU(machine.getIWMCU().toString());
			    damage.setQ32UKIDP((iwukidp != null) ? machine.getIWUKIDP() : 0l);
			    damage.setQ32UPID("DLAD");
			    damage.setQ32UPMJ(new Date());
			    damage.setQ32UPMB("0");
			    damage.setDuration(damage.getDuration() * 60);
			    damage.setDeleted(Boolean.FALSE);
			    damage.setNote("");

			    damage = damageDao.saveAndFlush(damage);

			    if (damage != null) {
				response = "Success insert damage with id:" + damage.getId();
			    }
			}
		    }
		}
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
	}

	return response;
    }

    /**
     * delete damage --> Return a success message for delete damage.
     *
     * @param id
     *            the id of damage
     * @return the success message or error message.
     */
    @RequestMapping("/damage/delete")
    @ResponseBody
    public String delete(Long id) {
	String response = null;

	try {
	    logger.log(Level.INFO, "Delete damage with id:{0}", id);

	    if (id != null) {
		Damage d = damageDao.findOne(id);

		if (d != null) {
		    d.setDeleted(Boolean.TRUE);
		    d = damageDao.saveAndFlush(d);

		    if (d.getDeleted().equals(Boolean.TRUE)) {
			response = "Success delete damage with id:" + d.getId();
		    }
		}
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
	}

	return response;
    }

    /**
     * restore damage --> Return a success message for restore damage.
     *
     * @param id
     *            the id of damage
     * @return the success message or error message.
     */
    @RequestMapping("/damage/restore")
    @ResponseBody
    public String restore(Long id) {
	String response = null;

	try {
	    logger.log(Level.INFO, "Restore damage with id:{0}", id);

	    if (id != null) {
		Damage d = damageDao.findOne(id);

		if (d != null) {
		    d.setDeleted(Boolean.FALSE);
		    d = damageDao.saveAndFlush(d);

		    if (d.getDeleted().equals(Boolean.FALSE)) {
			response = "Success restore damage with id:" + d.getId();
		    }
		}
	    }
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
	}

	return response;
    }

    // ------------------------
    // PRIVATE FIELDS
    // ------------------------
    @Autowired
    private DamageDao damageDao;

    @Autowired
    private UserDepartmentDao userDepartmentDao;

    @Autowired
    private CauseTypeDao causeTypeDao;

    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    private MachineDao machineDao;

    @Autowired
    private CauseDao causeDao;

    @Autowired
    private DelayDao delayDao;

    @Autowired
    private SubcauseDao subcauseDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private HttpServletRequest request;

    // ------------------------
    // PRIVATE METHODS
    // ------------------------
    private void setDamageInfo(Damage damage) {
	CauseType ct = causeTypeDao.findOne(damage.getType());
	Department d = departmentDao.findOne(damage.getDepartment());
	Machine m = machineDao.findOne(damage.getMachine());
	User u = userDao.findOne(damage.getUser());
	String cause = getCauseDescription(damage.getType(), damage.getCause(), damage.getDepartment());
	String subcause = getSubcauseDescription(damage.getType(), damage.getCause());
	// double fDuration = damage.getDuration() / 60.;
	Long lDuration = damage.getDuration() / 60;
	// double diff = fDuration - lDuration;

	// damage.setMinuteDuration(diff < 0.5 ? lDuration : lDuration + 1);
	damage.setMinuteDuration(lDuration);
	damage.setSecondsDuration(damage.getDuration() % 60);
	damage.setDescriptionType((ct != null) ? ct.getViewName() : "");
	damage.setDescriptionDepartment((d != null) ? d.getDescription() : "");
	damage.setDescriptionMachine((m != null) ? m.getCode() : "");
	damage.setDescriptionCause(!Strings.isNullOrEmpty(cause) ? cause : "");
	damage.setDescriptionSubcause(!Strings.isNullOrEmpty(subcause) ? subcause : "");
	damage.setDescriptionUser((u != null) ? u.getName() : "");
    }

    private String getCauseDescription(final Long causeType, final Long causeId, final Long department) {
	String description = null;

	if (causeType != null) {
	    if (!causeType.equals(CauseTypeEnum.DELAY.getId())) {
		Subcause sc = subcauseDao.findOne(causeId);
		Cause c = (sc != null) ? causeDao.findOne(sc.getCause()) : null;

		description = (c != null) ? c.getDescription() : "";
	    } else {
                try{
                    Delay d = delayDao.findByIdAndDepartment(causeId, department);
                    description = (d != null) ? d.getDescription() : "";
                }catch (Exception ex) {
                    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
                }
	    }
	}

	return description;
    }

    private String getSubcauseDescription(final Long causeType, final Long causeId) {
	String description = null;

	if (causeType != null) {
	    if (!causeType.equals(CauseTypeEnum.DELAY.getId())) {
		Subcause c = subcauseDao.findOne(causeId);

		description = (c != null) ? c.getDescription() : "";
	    }
	}

	return description;
    }

    @SuppressWarnings({ "null", "Unused`Assignment" })
    private List<Damage> getDamageSpecific(Integer criteriaType, String fieldSort) throws Exception {
	try {
	    ObjectMapper mapper = new ObjectMapper();
	    BufferedReader reader = request.getReader();
	    String jsonCriteria = "", line;

	    if (reader.ready()) {
		while ((line = reader.readLine()) != null) {
		    jsonCriteria += line;
		}
		reader.close();

		logger.log(Level.INFO, "Filtering damage with info:{0}", jsonCriteria);

		if (!Strings.isNullOrEmpty(jsonCriteria)) {
		    criteria = mapper.readValue(jsonCriteria, Criteria.class);

		    if (criteria != null) {
			List<Specification<Damage>> specs = new ArrayList<>();
			Specification<Damage> spec = departments(criteria.getDepartments());
			List<Long> types = new ArrayList<>();
			Long[] causes = null;

			if (criteriaType.equals(CriteriaType.DAMAGE)) {
			    types = Arrays.asList(criteria.getTypes());
			} else if (criteriaType.equals(CriteriaType.PARETO)) {
			    if (criteria.getTypes() == null || criteria.getTypes().length == 0) {
				types = Arrays.asList(new Long[] { CauseTypeEnum.ELECRTICAL.getId(),
					CauseTypeEnum.MECHANICAL.getId(), CauseTypeEnum.DELAY.getId() });
			    } else {
				types.addAll(Arrays.asList(criteria.getTypes()));
			    }
			}

			if (criteria.getCauses() != null && criteria.getSubcauses().length == 0) {
			    List<Subcause> subcauses = new ArrayList<>();

			    subcauses.addAll(subcauseDao.findByCauseInAndEnableOrderByDescriptionAsc(
				    Arrays.asList(criteria.getCauses()), Boolean.TRUE));

			    if (subcauses != null && !subcauses.isEmpty()) {
				causes = new Long[subcauses.size()];
				Integer index = 0;

				for (Subcause subcause : subcauses) {
				    causes[index++] = subcause.getId();
				}
			    }
			} else if (criteria.getSubcauses().length > 0) {
			    causes = criteria.getSubcauses();
			}

			paretoFrom = criteria.getFrom();
			paretoTo = criteria.getTo();
			if (spec != null) {
			    specs.add(spec);
			}
			spec = machines(criteria.getMachines());
			if (spec != null) {
			    specs.add(spec);
			}
			spec = types(types.toArray(new Long[types.size()]));
			if (spec != null) {
			    specs.add(spec);
			}
			spec = causes(causes);
			if (spec != null) {
			    specs.add(spec);
			}
			spec = users(criteria.getUsers());
			if (spec != null) {
			    specs.add(spec);
			}
			spec = between(criteria.getFrom(), criteria.getTo());

			if (spec != null) {
			    switch (specs.size()) {
			    case 1:
				spec = Specifications.where(spec).and(deleted()).and(specs.get(0));
				break;
			    case 2:
				spec = Specifications.where(spec).and(deleted()).and(specs.get(0)).and(specs.get(1));
				break;
			    case 3:
				spec = Specifications.where(spec).and(deleted()).and(specs.get(0)).and(specs.get(1))
					.and(specs.get(2));
				break;
			    case 4:
				spec = Specifications.where(spec).and(deleted()).and(specs.get(0)).and(specs.get(1))
					.and(specs.get(2)).and(specs.get(3));
				break;
			    case 5:
				spec = Specifications.where(spec).and(deleted()).and(specs.get(0)).and(specs.get(1))
					.and(specs.get(2)).and(specs.get(3)).and(specs.get(4));
				break;
			    default:
				spec = Specifications.where(spec).and(deleted());
			    }

			    return damageDao.findAll(spec, new Sort(Sort.Direction.DESC, fieldSort));
			}
		    }
		}
	    }
	} catch (IOException ex) {
	    logger.log(Level.SEVERE, null, ex);
	}

	return null;
    }

    @SuppressWarnings("null")
    private Double getDelaySpecific() throws Exception {
	if (criteria != null) {
	    List<Specification<Damage>> specs = new ArrayList<>();
	    Specification<Damage> spec = departments(criteria.getDepartments());
	    List<Long> types = Arrays.asList(CauseTypeEnum.DELAY.getId());

	    paretoFrom = criteria.getFrom();
	    paretoTo = criteria.getTo();
	    if (spec != null) {
		specs.add(spec);
	    }
	    spec = machines(criteria.getMachines());
	    if (spec != null) {
		specs.add(spec);
	    }
	    spec = types(types.toArray(new Long[types.size()]));
	    if (spec != null) {
		specs.add(spec);
	    }
	    spec = users(criteria.getUsers());
	    if (spec != null) {
		specs.add(spec);
	    }
	    spec = between(criteria.getFrom(), criteria.getTo());

	    if (spec != null) {
		switch (specs.size()) {
		case 1:
		    spec = Specifications.where(spec).and(deleted()).and(specs.get(0));
		    break;
		case 2:
		    spec = Specifications.where(spec).and(deleted()).and(specs.get(0)).and(specs.get(1));
		    break;
		case 3:
		    spec = Specifications.where(spec).and(deleted()).and(specs.get(0)).and(specs.get(1))
			    .and(specs.get(2));
		    break;
		case 4:
		    spec = Specifications.where(spec).and(deleted()).and(specs.get(0)).and(specs.get(1))
			    .and(specs.get(2)).and(specs.get(3));
		    break;
		case 5:
		    spec = Specifications.where(spec).and(deleted()).and(specs.get(0)).and(specs.get(1))
			    .and(specs.get(2)).and(specs.get(3)).and(specs.get(4));
		    break;
		default:
		    spec = Specifications.where(spec).and(deleted());
		}

		List<Damage> damages = damageDao.findAll(spec);

		if (damages != null && !damages.isEmpty()) {
		    Long duration = 0l;

		    duration = damages.stream().map((damage) -> damage.getDuration()).reduce(duration,
			    (accumulator, _item) -> accumulator + _item);

		    return duration / 60.0;
		}
	    }
	}

	return null;
    }

    private Double getPercent(Double currentDuration, Double prevDuration, Double totalDuration) {
	Double percent = 0.0;

	if (currentDuration != null && totalDuration != null) {
	    percent = (prevDuration != null) ? ((currentDuration + prevDuration) / totalDuration) * 100.0
		    : (currentDuration / totalDuration) * 100.0;
	    percent = (percent.compareTo(100.0) > 0) ? 100.0 : percent;
	}

	return new BigDecimal(percent).setScale(2, RoundingMode.CEILING).doubleValue();
    }

    private Double getTotalDuration(List<Damage> damages) {
	Double totalDuration = 0.0;

	for (Damage damage : damages) {
            if (!damage.getType().equals(CauseTypeEnum.DELAY.getId())) {
                Subcause subcause = subcauseDao.findOne(damage.getCause());

                if (subcause != null) {
                    totalDuration += damage.getDuration();
                }
            }else{
                try{
                    Delay delay = delayDao.findByIdAndDepartment(damage.getCause(), damage.getDepartment());
                    if (delay != null) {
                        totalDuration += damage.getDuration();
                    }
                }catch (Exception ex) {
                    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
                }
            }
	    
	}

	return totalDuration / 60.0;
    }

    private Long getTotalCause(List<Damage> damages) {
	Long totalCause = 0l;

	for (Damage damage : damages) {
	    if (!damage.getType().equals(CauseTypeEnum.DELAY.getId())) {
		Subcause subcause = subcauseDao.findOne(damage.getCause());

		if (subcause != null) {
		    totalCause++;
		}
	    }else{
                try{
                    Delay delay = delayDao.findByIdAndDepartment(damage.getCause(), damage.getDepartment());
                    if (delay != null) {
                        totalCause++;
                    }
                }catch (Exception ex) {
                    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
                }
            }
	}

	return (!totalCause.equals(0l)) ? totalCause : 1l;
    }

    private Long getMachinesByDamages(List<Damage> damages) {
	Long machines = 0l;
	Long machineId = 0l;

	Collections.sort(damages, (a, b) -> b.getMachine().compareTo(a.getMachine()));
	for (Damage damage : damages) {
	    if (!damage.getMachine().equals(machineId)) {
		machines++;
		machineId = damage.getMachine();
	    }
	}

	return (machines.equals(0l)) ? 1 : machines;
    }

    private String getMachineCodes(List<Damage> damages) {
	String codes = "";
	List<String> lstCodes = new ArrayList<>();
	Long machineId = 0l;

	Collections.sort(damages, (a, b) -> b.getMachine().compareTo(a.getMachine()));
	for (Damage damage : damages) {
	    if (!damage.getMachine().equals(machineId)) {
		Machine m = machineDao.findOne(damage.getMachine());

		if (m != null) {
		    lstCodes.add(m.getCode());
		}
		machineId = damage.getMachine();
	    }
	}
	Collections.sort(lstCodes, (a, b) -> a.compareTo(b));
	if (!lstCodes.isEmpty()) {
	    codes = lstCodes.stream().map((code) -> code + ",").reduce(codes, String::concat);
	}

	codes = (!Strings.isNullOrEmpty(codes)) ? codes.substring(0, codes.lastIndexOf(",")) : "";

	return codes;
    }

    private Long getDelayDurationCurrentShift(Date from, Date to, List departments, Long machine) throws Exception {
	Long duration = 0l;
	List<Damage> damages = (departments != null && !departments.isEmpty())
		? damageDao.findByCreatedBetweenAndDepartmentInAndTypeAndDeletedOrderByCreatedDesc(from, to,
			departments, CauseTypeEnum.DELAY.getId(), Boolean.FALSE)
		: (machine != null) ? damageDao.findByCreatedBetweenAndMachineAndTypeAndDeletedOrderByCreatedDesc(from,
			to, machine, CauseTypeEnum.DELAY.getId(), Boolean.FALSE) : null;

	if (damages != null && !damages.isEmpty()) {
	    duration = damages.stream().map((damage) -> damage.getDuration()).reduce(duration,
		    (accumulator, _item) -> accumulator + _item);
	}

	return duration;
    }

    private Boolean requestDelayOnly(Long[] types) {
	return (types != null && types.length == 1 && types[0].equals(CauseTypeEnum.DELAY.getId()));
    }
} // class DamageController
