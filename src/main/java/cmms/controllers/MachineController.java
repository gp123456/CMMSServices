/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.controllers;

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
 *
 * @author gpatitakis
 */
@Controller
public class MachineController {

    private static final Logger logger = Logger.getLogger(MachineController.class.getName());

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    /**
     * all --> Return all machines of db in JSON format.
     *
     * @return the db machines info or error message.
     */
    @RequestMapping("/machine/all")
    @ResponseBody
    public String all() {
        logger.log(Level.INFO, "Get all machines");

        String response = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            Collection<Machine> machines = (Collection<Machine>) machineDao.findAll(new Sort(Sort.Direction.ASC, "code"));
            ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Machine.class));

            if (machines != null && !machines.isEmpty()) {
                response = typedWriter.writeValueAsString(machines);
            } else {
                logger.log(Level.INFO, "There weren't machines");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
        }

        return response;
    }

    /**
     * all --> Return all machines by department of db in JSON format.
     *
     * @param id the department of plant
     * @return the db machine info for specific department or error message.
     */
    @RequestMapping("/machine/department")
    @ResponseBody
    @SuppressWarnings("null")
    public String department(String[] id) {
        logger.log(Level.INFO, "Get all machines for department id size:{0}", id.length);

        String response = null;

        try {
            if (id != null && id.length > 0) {
                List<Long> departments = new ArrayList<>();

                for (String id1 : id) {
                    departments.add(Long.parseLong(id1));
                }
                ObjectMapper mapper = new ObjectMapper();
                Collection<Machine> machines = (Collection<Machine>) machineDao.findByDepartmentInOrderByCodeAsc(departments);
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Machine.class));

                if (machines != null && !machines.isEmpty()) {
                    response = typedWriter.writeValueAsString(machines);
                } else {
                    logger.log(Level.INFO, "There weren't machines for the department:{0}", id);
                }
            } else {
                logger.log(Level.INFO, "The ids is empty:{0}", id);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
        }

        return response;
    }

    /**
     * all --> Return all machines by user of db in JSON format.
     *
     * @param id the user id from login in system
     * @return the db machine info for specific user or error message.
     */
    @RequestMapping("/machine/user")
    @ResponseBody
    public String user(String id) {
        logger.log(Level.INFO, "Get all machines for user id:{0}", id);

        String response = null;

        try {
            if (id != null) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Machine.class));
                Collection<UserDepartment> uds = userDepartmentDao.findByUsers(id);

                if (uds != null && !uds.isEmpty()) {
                    List<Long> departments = new ArrayList<>();

                    uds.stream().forEach((ud) -> {
                        departments.add(ud.getDepartment());
                    });

                    Collection<Machine> machines = machineDao.findByDepartmentInOrderByCodeAsc(departments);

                    if (machines != null && !machines.isEmpty()) {
                        response = typedWriter.writeValueAsString(machines);
                    } else {
                        response = "There weren't departments";
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
        }

        return response;
    }

    /**
     * one --> Return the machines by id of db in JSON format.
     *
     * @param id the machine id from login in system
     * @return the db machine info for specific user or error message.
     */
    @RequestMapping("/machine")
    @ResponseBody
    public String machine(Long id) {
        logger.log(Level.INFO, "Get the machine for id:{0}", id);

        String response = null;

        try {
            if (id != null) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Machine.class));
                Machine machine = machineDao.findOne(id);

                response = (machine != null) ? typedWriter.writeValueAsString(Arrays.asList(machine)) : "There weren't machine with id:" + id;
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
}
