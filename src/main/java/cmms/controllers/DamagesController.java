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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    /**
     * all --> Return all damage by user department(s) of db in JSON format.
     *
     * @param id the user id
     * @return the db damage info or error message.
     */
    @RequestMapping("/damage/user")
    @ResponseBody
    @SuppressWarnings("null")
    public String user(String id) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date from = null;

        try {
            from = sdf.parse(Shift.getStart(new Date()));
        } catch (ParseException ex) {
            Logger.getLogger(DamagesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        logger.log(Level.INFO, "Get all damages for all department(s) of user id:{0} and current shift:[{1},{2}] ",
                new Object[]{id, from.toString(), new Date().toString()});

        String response = null;

        try {
            if (id != null) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Damage.class));
                Collection<UserDepartment> uds = userDepartmentDao.findByUsers(id);

                if (uds != null && !uds.isEmpty()) {
                    List<Long> departments = new ArrayList<>();

                    uds.stream().forEach((ud) -> {
                        departments.add(ud.getDepartment());
                    });

                    Collection<Damage> damages = damageDao.findByCreatedBetweenAndDepartmentInAndDeletedOrderByCreatedDesc(
                            from,
                            new Date(),
                            departments,
                            Boolean.FALSE);

                    if (damages != null && !damages.isEmpty()) {
                        damages.stream().forEach((damage) -> {
                            setDamageInfo(damage);
                        });
                        response = typedWriter.writeValueAsString(damages);
                    } else {
                        logger.log(Level.INFO, "There weren't damages for the user department(s) id:{0}", id);
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
     * @param id the id of machine
     * @return the db damage info or error message.
     */
    @RequestMapping("/damage/machine")
    @ResponseBody
    @SuppressWarnings("null")
    public String machine(Long id) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date from = null;

        try {
            from = sdf.parse(Shift.getStart(new Date()));
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        logger.log(Level.INFO, "Get all damages for machine id:{0} and current shift:[{1},{2}] ",
                new Object[]{id, from.toString(), new Date().toString()});

        String response = null;

        try {
            if (id != null) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Damage.class));
                Collection<Damage> damages = damageDao.findByCreatedBetweenAndMachineAndDeletedOrderByCreatedDesc(from, new Date(), id, Boolean.FALSE);

                if (damages != null && !damages.isEmpty()) {
                    damages.stream().forEach((Damage damage) -> {
                        setDamageInfo(damage);
                    });
                    response = typedWriter.writeValueAsString(damages);
                } else {
                    logger.log(Level.INFO, "There weren't damages for the department:{0}", id);
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
     * @param id the id of department
     * @return the db damage info or error message.
     */
    @RequestMapping("/damage/department")
    @ResponseBody
    @SuppressWarnings("null")
    public String department(Long id) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date from = null;

        try {
            from = sdf.parse(Shift.getStart(new Date()));
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        logger.log(Level.INFO, "Get all damages for department id:{0} and current shift:[{1},{2}]",
                new Object[]{id, from.toString(), new Date().toString()});

        String response = null;

        try {
            if (id != null) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Damage.class));
                Collection<Damage> damages = damageDao.findByCreatedBetweenAndDepartmentInAndDeletedOrderByCreatedDesc(
                        from,
                        new Date(),
                        Arrays.asList(id),
                        Boolean.FALSE);

                if (damages != null && !damages.isEmpty()) {
                    damages.stream().forEach((Damage damage) -> {
                        setDamageInfo(damage);
                    });
                    response = typedWriter.writeValueAsString(damages);
                } else {
                    logger.log(Level.INFO, "There weren't damages for the department:{0}", id);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex);
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
                Long criteriaDuration = (paretoTo.getTime() - paretoFrom.getTime()) / 60000;
                ObjectMapper mapper = new ObjectMapper();
                List<Pareto> paretos = new ArrayList<>();
                Long causeId = 0l, sum = 0l, totalDuration = 0l, totalCauses = 0l;

                for (Damage damage : damages) {
                    if (!causeId.equals(damage.getCause())) {
                        if (!causeId.equals(0l)) {
                            Subcause subcause = subcauseDao.findOne(causeId);

                            if (subcause != null) {
                                paretos.add(new Pareto(subcause.getDescription(), sum));
                            }
                            totalCauses++;
                        }
                        causeId = damage.getCause();
                        sum = damage.getDuration();
                        totalDuration += damage.getDuration();
                    } else {
                        sum += damage.getDuration();
                        totalDuration += damage.getDuration();
                    }
                }
                Subcause subcause = subcauseDao.findOne(causeId);

                if (subcause != null) {
                    paretos.add(new Pareto(subcause.getCode(), sum));
                }

                response = mapper.writeValueAsString(new DepartmentPareto(
                        1l,
                        "current filter",
                        paretos,
                        new BigDecimal((totalDuration / 60) / totalCauses),
                        new BigDecimal((criteriaDuration - (totalDuration / 60)) / totalCauses)
                ));
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
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

        try {
            List<Damage> damages = getDamageSpecific(CriteriaType.DAMAGE, "created");
            ObjectMapper mapper = new ObjectMapper();

            if (damages != null && !damages.isEmpty()) {
                damages.stream().forEach((damage) -> {
                    setDamageInfo(damage);
                });
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Damage.class));

                response = typedWriter.writeValueAsString(damages);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
        }

        return response;
    }

    /**
     * list --> Return a list of summaries of damages by department of specific user in JSON format.
     *
     * @param user
     * @return the db cause types info or error message.
     */
    @RequestMapping("/damage/pareto/department")
    @ResponseBody
    public String paretoDepartment(String user) {
        logger.log(Level.INFO, "Get pareto of damages per department with user id:{0}", user);

        String response = null;

        try {
            if (user != null) {
                ObjectMapper mapper = new ObjectMapper();
                List<UserDepartment> uds = userDepartmentDao.findByUsers(user);

                if (uds != null && !uds.isEmpty()) {
                    try {
                        List<Long> departmentsId = new ArrayList<>();
                        uds.stream().forEach((ud) -> {
                            departmentsId.add(ud.getDepartment());
                        });
                        List<Pareto> paretos = new ArrayList<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date from = sdf.parse(Shift.getStart(new Date()));
                        Long totalDuration = 0l;
                        List<Object[]> sumDurationCauses = damageDao.sumDurationByDepartmentInShift(departmentsId, from, new Date());

                        if (sumDurationCauses != null && !sumDurationCauses.isEmpty()) {
                            for (Object[] values : sumDurationCauses) {
                                Subcause subcause = subcauseDao.findOne((Long) values[0]);

                                if (subcause != null) {
                                    paretos.add(new Pareto(subcause.getDescription(), (Long) values[1]));
                                    totalDuration += (Long) values[1];
                                }
                            }
                            response = mapper.writeValueAsString(new DepartmentPareto(
                                    Long.valueOf(departmentsId.size()),
                                    "login user:" + user,
                                    paretos,
                                    new BigDecimal((totalDuration / 60) / sumDurationCauses.size()),
                                    new BigDecimal((Shift.SHIFT_DURATION_MIN - (totalDuration / 60)) / sumDurationCauses.size())
                            ));
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
     * list --> Return a list of summaries of damages by department id in JSON format.
     *
     * @param id
     * @return the db cause types info or error message.
     */
    @RequestMapping("/damage/pareto/departmentId")
    @ResponseBody
    public String paretoDepartmentId(Long id) {
        logger.log(Level.INFO, "Get pareto of damages per department with id:{0}", id);

        String response = null;

        try {
            if (id != null) {
                ObjectMapper mapper = new ObjectMapper();
                
                try {
                    List<Pareto> paretos = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date from = sdf.parse(Shift.getStart(new Date()));
                    Long totalDuration = 0l;
                    List<Object[]> sumDurationCauses = damageDao.sumDurationByDepartmentInShift(Arrays.asList(id), from, new Date());

                    if (sumDurationCauses != null && !sumDurationCauses.isEmpty()) {
                        for (Object[] values : sumDurationCauses) {
                            Subcause subcause = subcauseDao.findOne((Long) values[0]);

                            if (subcause != null) {
                                paretos.add(new Pareto(subcause.getDescription(), (Long) values[1]));
                                totalDuration += (Long) values[1];
                            }
                        }
                        response = mapper.writeValueAsString(new DepartmentPareto(
                                id,
                                "",
                                paretos,
                                new BigDecimal((totalDuration / 60) / sumDurationCauses.size()),
                                new BigDecimal((Shift.SHIFT_DURATION_MIN - (totalDuration / 60)) / sumDurationCauses.size())
                        ));
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            } else {
                logger.log(Level.INFO, "no found damage with department id{0}", id);
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
     * @return the db cause types info or error message.
     */
    @RequestMapping("/damage/pareto/machine")
    @ResponseBody
    public String paretoMachine(Long id) {
        logger.log(Level.INFO, "Get pareto damages per machine with id:{0}", id);

        String response = null;

        try {
            if (id != null) {
                ObjectMapper mapper = new ObjectMapper();
                Machine machine = machineDao.findOne(id);

                if (machine != null) {
                    try {
                        List<Pareto> paretos = new ArrayList<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date from = sdf.parse(Shift.getStart(new Date()));
                        Long totalDuration = 0l;
                        List<Object[]> sumDurationCauses = damageDao.sumDurationByMachineShift(machine.getId(), from, new Date());

                        if (sumDurationCauses != null && !sumDurationCauses.isEmpty()) {
                            for (Object[] values : sumDurationCauses) {
                                Subcause subcause = subcauseDao.findOne((Long) values[0]);

                                if (subcause != null) {
                                    paretos.add(new Pareto(subcause.getDescription(), (Long) values[1]));
                                    totalDuration += (Long) values[1];
                                }
                            }
                            response = mapper.writeValueAsString(new DepartmentPareto(
                                    machine.getId(),
                                    machine.getCode(),
                                    paretos,
                                    new BigDecimal((totalDuration / 60) / sumDurationCauses.size()),
                                    new BigDecimal((Shift.SHIFT_DURATION_MIN - (totalDuration / 60)) / sumDurationCauses.size())
                            ));
                        }
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, null, ex);
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
                                d.setUser(damage.getUser());
                            }
                            if (!Strings.isNullOrEmpty(damage.getNote()) && !damage.getNote().equals(d.getNote())) {
                                d.setNote(damage.getNote());
                            }
                            if (damage.getCause() != null) {
                                d.setCause(damage.getCause());
                            }

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
                            damage.setUser("0");
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
     * @param id the id of damage
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
     * @param id the id of damage
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

                    if (d.getDeleted().equals(Boolean.TRUE)) {
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
        String cause = getCauseDescription(damage.getType(), damage.getCause());
        String subcause = getSubcauseDescription(damage.getType(), damage.getCause());
        double fDuration = damage.getDuration() / 60.;
        Long lDuration = damage.getDuration() / 60;
        double diff = fDuration - lDuration;

        damage.setMinuteDuration(diff < 0.5 ? lDuration : lDuration + 1);
        damage.setDescriptionType((ct != null) ? ct.getViewName() : "");
        damage.setDescriptionDepartment((d != null) ? d.getDescription() : "");
        damage.setDescriptionMachine((m != null) ? m.getCode() : "");
        damage.setDescriptionCause(!Strings.isNullOrEmpty(cause) ? cause : "");
        damage.setDescriptionSubcause(!Strings.isNullOrEmpty(subcause) ? subcause : "");
        damage.setDescriptionUser((u != null) ? u.getName() : "");
    }

    private String getCauseDescription(final Long causeType, final Long causeId) {
        String description = null;

        if (causeType != null) {
            if (!causeType.equals(CauseTypeEnum.DELAY.getId())) {
                Subcause sc = subcauseDao.findOne(causeId);
                Cause c = (sc != null) ? causeDao.findOne(sc.getCause()) : null;

                description = (c != null) ? c.getDescription() : "";
            } else {
                Delay d = delayDao.findOne(causeId);

                description = (d != null) ? d.getDescription() : "";
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

    @SuppressWarnings("null")
    private List<Damage> getDamageSpecific(Integer criteriaType, String fieldSort) {
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
                    Criteria criteria = mapper.readValue(jsonCriteria, Criteria.class);

                    logger.log(Level.INFO, "Criteria info:{0}", criteria.toString());

                    if (criteria != null) {
                        List<Specification<Damage>> specs = new ArrayList<>();
                        Specification<Damage> spec = departments(criteria.getDepartments());
                        List<Long> types = (criteriaType.equals(CriteriaType.DAMAGE))
                                ? Arrays.asList(criteria.getTypes())
                                : (criteriaType.equals(CriteriaType.PARETO) && (criteria.getTypes() == null || criteria.getTypes().length == 0))
                                ? Arrays.asList(new Long[]{CauseTypeEnum.ELECRTICAL.getId(), CauseTypeEnum.MECHANICAL.getId()})
                                : Arrays.asList(criteria.getTypes());

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
                        spec = causes(criteria.getSubcauses());
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
                                    spec = Specifications
                                            .where(spec)
                                            .and(deleted())
                                            .and(specs.get(0));
                                    break;
                                case 2:
                                    spec = Specifications
                                            .where(spec)
                                            .and(deleted())
                                            .and(specs.get(0))
                                            .and(specs.get(1));
                                    break;
                                case 3:
                                    spec = Specifications
                                            .where(spec)
                                            .and(deleted())
                                            .and(specs.get(0))
                                            .and(specs.get(1))
                                            .and(specs.get(2));
                                    break;
                                case 4:
                                    spec = Specifications
                                            .where(spec)
                                            .and(deleted())
                                            .and(specs.get(0))
                                            .and(specs.get(1))
                                            .and(specs.get(2))
                                            .and(specs.get(3));
                                    break;
                                case 5:
                                    spec = Specifications
                                            .where(spec)
                                            .and(deleted())
                                            .and(specs.get(0))
                                            .and(specs.get(1))
                                            .and(specs.get(2))
                                            .and(specs.get(3))
                                            .and(specs.get(4));
                                    break;
                                default:
                                    Specifications.where(spec).and(deleted());
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
    private Map<Long, List<Damage>> getDamages(String field, List<Damage> damages) {
        Map<Long, List<Damage>> response = null;

        damages.stream().forEach((damage) -> {
            @SuppressWarnings("null")
            List<Damage> d = (field.equals("department"))
                    ? response.get(damage.getDepartment())
                    : (field.equals("cause"))
                    ? response.get(damage.getCause())
                    : null;

            if (d != null && !d.isEmpty()) {
                d.add(damage);
            } else {
                Long key = (field.equals("department"))
                        ? damage.getDepartment()
                        : (field.equals("cause"))
                        ? damage.getCause()
                        : null;

                if (key != null) {
                    response.put(key, new ArrayList<>(Arrays.asList(damage)));
                }
            }
        });

        return response;
    }

    private Long sumDurationDamages(List<Damage> damages) {
        Long sum = 0l;

        sum = damages.stream().map((damage) -> damage.getDuration()).reduce(sum, (accumulator, _item) -> accumulator + _item);

        return sum;
    }
} // class DamageController
