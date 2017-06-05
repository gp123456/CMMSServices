package cmms.controllers;

import cmms.dao.CauseTypeDao;
import cmms.dao.DamageDao;
import cmms.dao.DepartmentDao;
import cmms.dao.MachineDao;
import cmms.dao.UserDao;
import cmms.dao.UserDepartmentDao;
import cmms.enums.CauseTypeEnum;
import cmms.enums.UserTypeEnum;
import cmms.helpers.Shift;
import cmms.models.CauseType;
import cmms.models.Department;
import cmms.models.Machine;
import cmms.models.User;
import cmms.models.UserDepartment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    @RequestMapping("/")
    @ResponseBody
    public String index() {
        return "Hello World";
    }

    @RequestMapping("/overview/department")
    @ResponseBody
    public String department(String userId) {
        String response = "no found records";

        logger.log(Level.INFO, "Overview department with user id:{0}", userId);

        try {
            if (userId != null) {
                User user = userDao.findOne(userId);

                if (user != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Department.class));
                    Collection<UserDepartment> uds = userDepartmentDao.findByUsers(userId);

                    if (uds != null && !uds.isEmpty()) {
                        List<Long> ids = new ArrayList<>();

                        uds.stream().forEach((ud) -> {
                            ids.add(ud.getDepartment());
                        });

                        List<Department> departments = departmentDao.findByIdInOrderByDescriptionAsc(ids);
                        @SuppressWarnings("UnusedAssignment")
                        List<CauseType> types;

                        if (user.getType().equals(UserTypeEnum.ELECTRICIAN.getId())) {
                            CauseType type = causeTypeDao.findOne(CauseTypeEnum.ELECRTICAL.getId());

                            types = (type != null) ? Arrays.asList(type) : null;
                        } else if (user.getType().equals(UserTypeEnum.ENGINEER.getId())) {
                            CauseType type = causeTypeDao.findOne(CauseTypeEnum.MECHANICAL.getId());

                            types = (type != null) ? Arrays.asList(type) : null;
                        } else {
                            types = causeTypeDao.findAll();
                        }
                        if (types != null && !types.isEmpty() && departments != null && !departments.isEmpty()) {
                            departments.stream().forEach((Department department) -> {
                                try {
                                    List<Machine> machines = machineDao.findByDepartmentOrderByCodeAsc(department.getId());

                                    if (machines != null && !machines.isEmpty()) {
                                        ids.clear();
                                        machines.stream().forEach((machine) -> {
                                            ids.add(machine.getId());
                                        });
                                        department.setCauses(setMachineCause(types, ids));
                                    }
                                } catch (Exception ex) {
                                    logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
                                }
                            });
                        }

                        if (departments != null && !departments.isEmpty()) {
                            response = typedWriter.writeValueAsString(departments);
                        }  else {
                            logger.log(Level.INFO, "There weren't departments");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
        }

        return response;
    }

    @RequestMapping("/overview/machine")
    @ResponseBody
    public String machine(Long departmentId, String userId) {
        String response = null;

        logger.log(Level.INFO, "Overview machines with department id:{0} and user id:{1}", new Object[]{departmentId, userId});

        try {
            if (departmentId != null && !Strings.isNullOrEmpty(userId)) {
                User user = userDao.findOne(userId);

                if (user != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Machine.class));
                    Collection<Machine> machines = machineDao.findByDepartmentOrderByCodeAsc(departmentId);

                    if (machines != null && !machines.isEmpty()) {
                        List<CauseType> types;

                        if (user.getType().equals(UserTypeEnum.ELECTRICIAN.getId())) {
                            CauseType type = causeTypeDao.findOne(CauseTypeEnum.ELECRTICAL.getId());

                            types = (type != null) ? Arrays.asList(type) : null;
                        } else if (user.getType().equals(UserTypeEnum.ENGINEER.getId())) {
                            CauseType type = causeTypeDao.findOne(CauseTypeEnum.MECHANICAL.getId());

                            types = (type != null) ? Arrays.asList(type) : null;
                        } else {
                            types = causeTypeDao.findAll();
                        }

                        if (types != null && !types.isEmpty()) {
                            machines.stream().forEach((machine) -> {
                                machine.setCauses(setMachineCause(types, machine.getId()));
                            });
                        }
                        response = typedWriter.writeValueAsString(machines);
                    } else {
                        logger.log(Level.INFO, "There weren't departments with id:{0} and user id:{1}", new Object[]{departmentId, userId});
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
        }

        return response;
    }

    // ------------------------
    // PRIVATE FUNCTIONS
    // ------------------------
    private String setMachineCause(final Collection<CauseType> types, final Long machineId) {
        String cause = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date from = null;

        try {
            from = sdf.parse(Shift.getStart(new Date()));
        } catch (ParseException ex) {
            Logger.getLogger(DamagesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (CauseType type : types) {
            try {
                Long damages = damageDao.countByCreatedBetweenAndMachineAndTypeAndDeleted(from, new Date(), machineId, type.getId(), Boolean.FALSE);

                if (damages != null) {
                    cause += type.getViewName() + ":" + damages + ",";
                } else {
                    cause += type.getViewName() + ":" + 0 + ",";
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return cause.substring(0, cause.lastIndexOf(','));
    }

    private String setMachineCause(final List<CauseType> types, final List machineId) {
        String cause = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date from = null;

        try {
            from = sdf.parse(Shift.getStart(new Date()));
        } catch (ParseException ex) {
            Logger.getLogger(DamagesController.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (CauseType type : types) {
            try {
                Long damages = damageDao.countByCreatedBetweenAndMachineInAndTypeAndDeleted(from, new Date(), machineId, type.getId(), Boolean.FALSE);

                if (damages != null) {
                    cause += type.getViewName() + ":" + damages + ",";
                } else {
                    cause += type.getViewName() + ":" + 0 + ",";
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return cause.substring(0, cause.lastIndexOf(','));
    }

// ------------------------
// PRIVATE FIELDS
// ------------------------
    @Autowired
    private UserDepartmentDao userDepartmentDao;

    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    private MachineDao machineDao;

    @Autowired
    private DamageDao damageDao;

    @Autowired
    private CauseTypeDao causeTypeDao;

    @Autowired
    private UserDao userDao;
}
