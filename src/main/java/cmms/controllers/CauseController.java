/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.controllers;

import cmms.dao.CauseDao;
import cmms.dao.CauseTypeDao;
import cmms.dao.DamageDao;
import cmms.dao.DelayDao;
import cmms.dao.DepartmentDao;
import cmms.dao.MachineDao;
import cmms.dao.SubcauseDao;
import cmms.dao.UserDao;
import cmms.dao.UserDepartmentDao;
import cmms.enums.CauseTypeEnum;
import cmms.enums.UserTypeEnum;
import cmms.models.Cause;
import cmms.models.CauseType;
import cmms.models.Damage;
import cmms.models.Delay;
import cmms.models.Department;
import cmms.models.Machine;
import cmms.models.RefCause;
import cmms.models.RefFirstCause;
import cmms.models.Subcause;
import cmms.models.User;
import cmms.models.UserDepartment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author gpatitakis
 */
@Controller
public class CauseController {

    private static final Logger logger = Logger.getLogger(CauseController.class.getName());

    private static final Long OFFSET_CAUSE_ID = 10000l;

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    /**
     * list --> Return list enabled causes of db in JSON format.
     *
     * @return the list db causes info or error message.
     */
    @RequestMapping("/cause/all")
    @ResponseBody
    public String all() {
        logger.log(Level.INFO, "Get all enabled causes");

        String response = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            Collection<Cause> causes = causeDao.findByEnableOrderByDescriptionAsc(Boolean.TRUE);
            ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Cause.class));

            if (causes != null && !causes.isEmpty()) {
                response = typedWriter.writeValueAsString(causes);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all causes in db with error:" + ex.getMessage();
        }

        return response;
    }

    /**
     * list --> Return list causes by department of db in JSON format.
     *
     * @param id
     * @return the list db causes info or error message.
     */
    @RequestMapping("/cause/department")
    @ResponseBody
    public String department(String[] id) {
        logger.log(Level.INFO, "Get list causes by department id size:{0}", Arrays.toString(id));

        String response = null;

        try {
            if (id != null && id.length > 0) {
                List<Long> departments = new ArrayList<>();

                for (String id1 : id) {
                    departments.add(Long.parseLong(id1));
                }

                ObjectMapper mapper = new ObjectMapper();
                Collection<Cause> causes = causeDao.findByDepartmentInAndEnableOrderByDescriptionAsc(departments, true);
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Cause.class));

                response = (causes != null && !causes.isEmpty())
                        ? typedWriter.writeValueAsString(causes)
                        : "There weren't causes for department id:" + Arrays.toString(id);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all causes in db with error:" + ex.getMessage();
        }

        return response;
    }

    /**
     * list --> Return list causes by type of db in JSON format.
     *
     * @param id
     * @return the list db causes info or error message.
     */
    @RequestMapping("/cause/type")
    @ResponseBody
    @SuppressWarnings("null")
    public String type(String[] id) {
        logger.log(Level.INFO, "Get list causes by type id:{0}", Arrays.toString(id));

        String response = null;

        try {
            if (id != null && id.length > 0) {
                List<Long> types = new ArrayList<>();
                List<Cause> causes = new ArrayList<>();

                for (String _id : id) {
                    Long __id = Long.parseLong(_id);

                    if (__id.equals(CauseTypeEnum.DELAY.getId())) {
                        Collection<Delay> delays = delayDao.findAll(new Sort(Sort.Direction.ASC, "description"));

                        if (delays != null && !delays.isEmpty()) {
                            delays.stream().forEach((delay) -> {
                                causes.add(new Cause(delay.getId(), 3l, delay.getDepartment(), true, null, delay.getDescription()));
                            });
                        }
                    } else {
                        types.add(__id);
                    }
                }

                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Cause.class));
                Collection<Cause> _causes = causeDao.findByTypeInAndEnableOrderByDescriptionAsc(types, true);

                if (_causes != null && !_causes.isEmpty()) {
                    _causes.stream().forEach((cause) -> {
                        cause.setId(OFFSET_CAUSE_ID + cause.getId());
                    });
                    causes.addAll(_causes);
                }
                response = (causes != null && !causes.isEmpty())
                        ? typedWriter.writeValueAsString(causes)
                        : "There weren't causes for type id:" + Arrays.toString(id);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all causes in db with error:" + ex.getMessage();
        }

        return response;
    }

    /**
     * list --> Return list causes by user of db in JSON format.
     *
     * @param id
     * @return the list db causes info or error message.
     */
    @RequestMapping("/cause/user")
    @ResponseBody
    public String user(String id) {
        logger.log(Level.INFO, "Get list causes by user id:{0}", id);

        String response = null;

        try {
            if (id != null) {
                User user = userDao.findOne(id);

                if (user != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Cause.class));
                    Collection<UserDepartment> uds = userDepartmentDao.findByUsers(id);

                    if (uds != null && !uds.isEmpty()) {
                        List<Long> departments = new ArrayList<>();

                        uds.stream().forEach((ud) -> {
                            departments.add(ud.getDepartment());
                        });
                        Collection<Cause> causes;

                        if (user.getType().equals(UserTypeEnum.ELECTRICIAN.getId())) {
                            causes = causeDao.findByTypeAndDepartmentInAndEnableOrderByDescriptionAsc(CauseTypeEnum.ELECRTICAL.getId(), departments, true);
                        } else if (user.getType().equals(UserTypeEnum.ENGINEER.getId())) {
                            causes = causeDao.findByTypeAndDepartmentInAndEnableOrderByDescriptionAsc(CauseTypeEnum.MECHANICAL.getId(), departments, true);
                        } else {
                            causes = causeDao.findByDepartmentInAndEnableOrderByDescriptionAsc(departments, true);
                        }

                        response = (causes != null && !causes.isEmpty())
                                ? typedWriter.writeValueAsString(causes)
                                : "There weren't causes for user id:" + id;
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all causes in db with error:" + ex.getMessage();
        }

        return response;
    }

    @RequestMapping("/cause/type-department")
    @ResponseBody
    @SuppressWarnings("null")
    public String type_department(@RequestParam("type") String[] types, @RequestParam("department") String[] departments) {
        logger.log(Level.INFO, "Get list causes by type id:{0} and department id{1}", new Object[]{Arrays.toString(types), Arrays.toString(departments)});

        String response = null;

        try {
            if (types != null && types.length > 0 && departments != null && departments.length > 0) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Cause.class));
                List<Long> typeIds = new ArrayList<>();
                List<Long> departmentIds = new ArrayList<>();
                List<Cause> causes = new ArrayList<>();

                for (String department : departments) {
                    departmentIds.add(Long.parseLong(department));
                }
                for (String type : types) {
                    Long _type = Long.parseLong(type);

                    if (_type.equals(CauseTypeEnum.DELAY.getId())) {
                        Collection<Delay> delays = delayDao.findByDepartmentInOrderByDescriptionAsc(departmentIds);

                        if (delays != null && !delays.isEmpty()) {
                            delays.stream().forEach((delay) -> {
                                causes.add(new Cause(delay.getId(), 3l, delay.getDepartment(), true, null, delay.getDescription()));
                            });
                        }
                    } else {
                        typeIds.add(_type);
                    }
                }
                List<Cause> _causes = causeDao.findByTypeInAndDepartmentInAndEnableOrderByDescriptionAsc(typeIds, departmentIds, true);
                if (_causes != null && !_causes.isEmpty()) {
                    _causes.stream().forEach((cause) -> {
                        cause.setId(OFFSET_CAUSE_ID + cause.getId());
                    });
                    causes.addAll(_causes);
                }
                response = (causes != null && !causes.isEmpty())
                        ? typedWriter.writeValueAsString(causes)
                        : "There weren't causes for type id:" + Arrays.toString(types) + " and department id:" + Arrays.toString(departments);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all causes in db with error:" + ex.getMessage();
        }

        return response;
    }

    @RequestMapping("/cause/type-user")
    @ResponseBody
    public String type_user(@RequestParam("type") Long type, @RequestParam("user") String user) {
        logger.log(Level.INFO, "Get list causes by type id:{0} and user id:{1}", new Object[]{type, user});

        String response = null;

        try {
            if (type != null && user != null) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = (type.equals(CauseTypeEnum.DELAY.getId()))
                        ? mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Delay.class))
                        : mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Cause.class));
                Collection<UserDepartment> uds = userDepartmentDao.findByUsers(user);

                if (uds != null && !uds.isEmpty()) {
                    List<Long> departments = new ArrayList<>();
                    List<Long> types = new ArrayList<>(Arrays.asList(type));

                    uds.stream().forEach((ud) -> {
                        departments.add(ud.getDepartment());
                    });
                    Collection<Object> causes = (type.equals(CauseTypeEnum.DELAY.getId()))
                            ? delayDao.findByDepartmentInOrderByDescriptionAsc(departments)
                            : causeDao.findByTypeInAndDepartmentInAndEnableOrderByDescriptionAsc(types, departments, true);

                    response = (causes != null && !causes.isEmpty())
                            ? typedWriter.writeValueAsString(causes)
                            : "There weren't causes for type id:" + type + " user id:" + user;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all causes in db with error:" + ex.getMessage();
        }

        return response;
    }

    @RequestMapping("/cause/machine")
    @ResponseBody
    public String machine(Long id) {
        logger.log(Level.INFO, "Get list causes by machine id:{0}", id);

        String response = null;

        try {
            if (id != null) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Cause.class));
                Machine machine = machineDao.getOne(id);

                if (machine != null) {
                    List causes = causeDao.findByDepartmentAndEnableOrderByDescriptionAsc(machine.getDepartment(), true);

                    response = (causes != null && !causes.isEmpty())
                            ? typedWriter.writeValueAsString(causes)
                            : "There weren't causes for machine id:" + id;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all causes in db with error:" + ex.getMessage();
        }

        return response;
    }

    @RequestMapping("/cause/damage")
    @ResponseBody
    public String damage(Long id) {
        logger.log(Level.INFO, "Get list causes by damage id:{0}", id);

        String response = null;

        try {
            if (id != null) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Cause.class));
                Damage damage = damageDao.getOne(id);

                if (damage != null) {
                    List causes = causeDao.findByTypeAndDepartmentAndEnableOrderByDescriptionAsc(damage.getType(), damage.getDepartment(), Boolean.TRUE);

                    response = (causes != null && !causes.isEmpty())
                            ? typedWriter.writeValueAsString(causes)
                            : "There weren't causes for machine id:" + id;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all causes in db with error:" + ex.getMessage();
        }

        return response;
    }

    @RequestMapping("/cause/complete")
    @ResponseBody
    public String complete() throws Exception {
        logger.log(Level.INFO, "Get all causes and subcauses");

        @SuppressWarnings("UnusedAssignment")
        String response = null;

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, RefCause.class));
        List<RefCause> values = null;
        List<Cause> causes = causeDao.findByEnableOrderByDescriptionAsc(Boolean.TRUE);

        if (causes != null && !causes.isEmpty()) {
            values = new ArrayList<>();

            for (Cause cause : causes) {
                List<Subcause> subcauses = subcauseDao.findByCauseAndEnableOrderByDescriptionAsc(cause.getId(), Boolean.TRUE);
                CauseType causeType = causeTypeDao.findOne(cause.getType());
                Department department = departmentDao.findOne(cause.getDepartment());

                if (subcauses != null && !subcauses.isEmpty()) {
                    for (Subcause subcause : subcauses) {
                        values.add(
                                new RefCause(
                                        department.getId(),
                                        department.getDescription(),
                                        causeType.getViewName(),
                                        cause.getId(),
                                        "<a class='cause-modify' id='cause-modify" + cause.getId() + "' data-id='" + cause.getId() + "'>"
                                        + cause.getDescription() + "</a>",
                                        "<a class='subcause-modify' id='subcause-modify" + subcause.getId() + "' data-id='" + subcause.getId() + "'>"
                                        + subcause.getDescription() + "</a>",
                                        subcause.getId()
                                )
                        );
                    }
                } else {
                    values.add(
                            new RefCause(
                                    department.getDescription(),
                                    causeType.getViewName(),
                                    "<a class='cause-modify' id='cause-modify" + cause.getId() + "' data-id='" + cause.getId() + "'>"
                                    + cause.getDescription() + "</a>",
                                    cause.getId()
                            )
                    );
                }
            }
        }

        response = (values != null && !values.isEmpty())
                ? typedWriter.writeValueAsString(values)
                : "There weren't causes";

        try {
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Get all causes and subcauses with error:" + ex.getMessage();
        }

        return response;
    }

    @RequestMapping("/cause/insert-first")
    @ResponseBody
    public String insertFirstLevel() throws Exception {
        String response = null;
        BufferedReader reader = request.getReader();
        String jsonRefCause = "", line;
        ObjectMapper mapper = new ObjectMapper();

        if (reader.ready()) {
            while ((line = reader.readLine()) != null) {
                jsonRefCause += line;
            }
        }

        logger.log(Level.INFO, "Insert new cause:{0}", jsonRefCause);

        if (!Strings.isNullOrEmpty(jsonRefCause)) {
            RefFirstCause refCause = mapper.readValue(jsonRefCause, RefFirstCause.class);

            if (refCause != null) {
                causeDao.saveAndFlush(new Cause(refCause.getType(), refCause.getDepartment(), Boolean.TRUE, refCause.getCode(), refCause.getCause()));
            }
        }

        return response;
    }

    @RequestMapping("/cause/insert")
    @ResponseBody
    public String insert() throws Exception {
        @SuppressWarnings("UnusedAssignment")
        String response = null;

        BufferedReader reader = request.getReader();
        String jsonRefCause = "", line;
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, RefCause.class));

        if (reader.ready()) {
            while ((line = reader.readLine()) != null) {
                jsonRefCause += line;
            }
        }

        logger.log(Level.INFO, "Insert new cause / subcause:{0}", jsonRefCause);

        if (!Strings.isNullOrEmpty(jsonRefCause)) {
            RefCause refCause = mapper.readValue(jsonRefCause, RefCause.class);

            if (refCause != null) {
                subcauseDao.saveAndFlush(new Subcause(refCause.getCauseId() - OFFSET_CAUSE_ID, Boolean.TRUE, null, refCause.getSubcause()));
            }
        }
        List<RefCause> values = null;
        List<Cause> causes = causeDao.findAll(new Sort(Sort.Direction.ASC, "description"));

        if (causes != null && !causes.isEmpty()) {
            values = new ArrayList<>();

            for (Cause cause : causes) {
                List<Subcause> subcauses = subcauseDao.findByCauseAndEnableOrderByDescriptionAsc(cause.getId(), Boolean.TRUE);
                CauseType causeType = causeTypeDao.findOne(cause.getType());
                Department department = departmentDao.findOne(cause.getDepartment());

                if (subcauses != null && !subcauses.isEmpty()) {
                    for (Subcause subcause : subcauses) {
                        values.add(
                                new RefCause(
                                        department.getId(),
                                        department.getDescription(),
                                        causeType.getViewName(),
                                        cause.getId(),
                                        "<a class='cause-modify' id='cause-modify" + cause.getId() + "' data-id='" + cause.getId() + "'>"
                                        + cause.getDescription() + "</a>",
                                        "<a class='subcause-modify' id='subcause-modify" + subcause.getId() + "' data-id='" + subcause.getId() + "'>"
                                        + subcause.getDescription()
                                        + "</a>",
                                        subcause.getId()
                                )
                        );
                    }
                } else {
                    values.add(
                            new RefCause(
                                    department.getDescription(),
                                    causeType.getViewName(),
                                    "<a class='cause-modify' id='cause-modify" + cause.getId() + "' data-id='" + cause.getId() + "'>"
                                    + cause.getDescription() + "</a>",
                                    cause.getId()
                            )
                    );
                }
            }
        }

        response = (values != null && !values.isEmpty())
                ? typedWriter.writeValueAsString(values)
                : "There weren't causes";

        try {
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Get all causes and subcauses with error:" + ex.getMessage();
        }

        return response;
    }

    /**
     * delete damage --> Return a success message for delete cause.
     *
     * @param id the id of cause
     * @return the success message or error message.
     */
    @RequestMapping("/cause/delete")
    @ResponseBody
    public String delete(Long id) {
        String response = null;

        try {
            logger.log(Level.INFO, "Delete cause with id:{0}", id);

            if (id != null) {
                Cause c = causeDao.findOne(id);

                if (c != null) {
                    c.setEnable(Boolean.FALSE);
                    c = causeDao.saveAndFlush(c);

                    if (c.isEnable().equals(Boolean.FALSE)) {
                        List<Subcause> subcauses = subcauseDao.findByCauseAndEnableOrderByDescriptionAsc(id, Boolean.TRUE);

                        if (subcauses != null && !subcauses.isEmpty()) {
                            for (Subcause subcause : subcauses) {
                                subcause.setEnable(Boolean.FALSE);
                                subcause = subcauseDao.saveAndFlush(subcause);
                            }
                        }

                        response = "Success delete cause with id:" + c.getId();
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
    private UserDepartmentDao userDepartmentDao;

    @Autowired
    private MachineDao machineDao;

    @Autowired
    private CauseDao causeDao;

    @Autowired
    private CauseTypeDao causeTypeDao;

    @Autowired
    private SubcauseDao subcauseDao;

    @Autowired
    private DelayDao delayDao;

    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    private DamageDao damageDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private HttpServletRequest request;
} // class CauseController
