package cmms.controllers;

import cmms.models.Department;
import cmms.dao.DepartmentDao;
import cmms.dao.MachineDao;
import cmms.dao.UserDepartmentDao;
import cmms.models.Machine;
import cmms.models.UserDepartment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * A class to test interactions with the MySQL database using the UserDao class.
 *
 * @author netgloo
 */
@Controller
public class DepartmentController {

    private static final Logger logger = Logger.getLogger(DepartmentController.class.getName());

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    /**
     * all --> Return all departments of db in json format.
     *
     * @return the db department info or error message.
     */
    @RequestMapping("/department/all")
    @ResponseBody
    public String all() {
        logger.log(Level.INFO, "Get all departments");

        String response;

        try {
            ObjectMapper mapper = new ObjectMapper();
            Collection<Department> departments = (Collection<Department>) departmentDao.findAll(new Sort(Sort.Direction.ASC, "Description"));
            ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Department.class));

            response = (departments != null && !departments.isEmpty())
                    ? typedWriter.writeValueAsString(departments)
                    : "There weren't departments";
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all departments in db with error:" + ex.getMessage();
        }

        return response;
    }

    /**
     * list --> Return the departments by user id of db in JSON format.
     *
     * @param id the user id
     * @return the db department info or error message.
     */
    @RequestMapping("/department/user")
    @ResponseBody
    public String user(String id) {
        logger.log(Level.INFO, "Get department with user id:{0}", id);

        String response = null;

        try {
            if (id != null) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Department.class));
                Collection<UserDepartment> uds = userDepartmentDao.findByUsers(id);

                if (uds != null && !uds.isEmpty()) {
                    List<Long> ids = new ArrayList<>();

                    uds.stream().forEach((ud) -> {
                        ids.add(ud.getDepartment());
                    });
                    Collection<Department> departments = departmentDao.findByIdInOrderByDescriptionAsc(ids);

                    response = (departments != null && !departments.isEmpty())
                            ? typedWriter.writeValueAsString(departments)
                            : "There weren't department with user id :" + id;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
        }

        return response;
    }

    /**
     * one --> Return the departments by machine id of db in JSON format.
     *
     * @param id the machine id
     * @return the db department info or error message.
     */
    @RequestMapping("/department/machine")
    @ResponseBody
    public String machine(Long id) {
        logger.log(Level.INFO, "Get department with machine id:{0}", id);

        String response = null;

        try {
            if (id != null) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Department.class));
                Machine m = machineDao.findOne(id);

                if (m != null) {
                    Department d = departmentDao.findOne(m.getDepartment());

                    if (d != null) {
                        response = typedWriter.writeValueAsString(Arrays.asList(d));
                    } else {
                        response = "There weren't department with machine id:" + id;
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
    private DepartmentDao departmentDao;

} // class UserController
