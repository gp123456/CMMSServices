package cmms.controllers;

import cmms.dao.DamageDao;
import cmms.models.User;
import cmms.dao.UserDao;
import cmms.dao.UserDepartmentDao;
import cmms.enums.CauseTypeEnum;
import cmms.enums.UserTypeEnum;
import cmms.models.Damage;
import cmms.models.UserDepartment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
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
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    /**
     * /create --> Create a new user and save it in the database.
     *
     * @return A string describing if the user is succesfully created or not.
     */
    @RequestMapping("/user/create")
    @ResponseBody
    public String create() {
        String response = null;

        try {
            BufferedReader reader = request.getReader();
            ObjectMapper mapper = new ObjectMapper();
            String jsonUser = "", line;

            if (reader.ready()) {
                while ((line = reader.readLine()) != null) {
                    jsonUser += line;
                }

                if (!Strings.isNullOrEmpty(jsonUser)) {
                    logger.log(Level.INFO, "User create:{0}", jsonUser);

                    User user = mapper.readValue(jsonUser, User.class);

                    if (user != null) {
                        User dbuser = userDao.findByUsernameAndPassword(user.getUsername(), user.getPassword());

                        if (dbuser == null) {
                            user.setABALPH("");
                            user.setABLOCN("");
                            dbuser = userDao.save(user);

                            if (dbuser != null) {
                                Long[] departments = user.getDepartments();
                                
                                if (departments != null && departments.length > 0) {
                                    for (Long department : departments) {
                                        userDepartmentDao.save(new UserDepartment(user.getId(), department));
                                    }
                                }
                                response = mapper.writeValueAsString(user);
                            }
                        } else {
                            response = "Exist User";
                        }
                    } else {
                        response = "No valided input info:" + jsonUser;
                    }
                } else {
                    response = "The input info was empty";
                }
            } else {
                response = "The Buffered Reader of HttpServletRequest wasn't ready";
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Error creating the user: " + ex.getMessage();
        }

        return response;
    }

    /**
     * /delete --> Delete the user having the passed id.
     *
     * @param id The id of the user to delete
     * @return A string describing if the user is succesfully deleted or not.
     */
    @RequestMapping("/user/delete")
    @ResponseBody
    public String delete(String id) {
        logger.log(Level.INFO, "User delete with id:{0}", id);

        String response = null;

        try {
            if (id != null) {
                User user = new User(id);

                userDao.delete(user);
                response = "User whith id:" + user.getId() + " succesfully deleted!";
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Error deleting the user: " + ex.getMessage();
        }

        return response;
    }

    /**
     * /get-by-username, password --> Return the id for the user having the passed username, password.
     *
     * @param username
     * @param password
     * @return the db user info or error message.
     */
    @RequestMapping("/user/login")
    @ResponseBody
    public String loginUser(String username, String password) {
        logger.log(Level.INFO, "User login with username:{0} and password:{1}", new Object[]{username, password});

        String response = null;

        try {
            if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
                ObjectMapper mapper = new ObjectMapper();
                User user = userDao.findByUsernameAndPassword(username, password);

                if (user != null) {
                    response = mapper.writeValueAsString(user);

                } else {
                    response = "No login user with username:" + username + " and password:" + password;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "User not found with error:" + ex.getMessage();
        }

        return response;
    }

    /**
     * /get-by-type --> Return the users having the passed type.
     *
     * @param id the type of user
     * @return the db user info for specific type or error message.
     */
    @RequestMapping("/user/type")
    @ResponseBody
    public String type(Integer id) {
        logger.log(Level.INFO, "Get users with type id:{0}", id);

        String response = null;

        try {
            if (id != null) {
                ObjectMapper mapper = new ObjectMapper();
                List users = (id.equals(UserTypeEnum.ELECTRICIAN.getId()) || id.equals(UserTypeEnum.ENGINEER.getId()))
                        ? userDao.findByTypeOrderByNameAsc(id) : userDao.findAll(new Sort(Sort.Direction.ASC, "name"));
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, User.class));

                if (users != null && !users.isEmpty()) {
                    response = typedWriter.writeValueAsString(users);

                } else {
                    response = "No found user with type id:" + id;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "User not found with error:" + ex.getMessage();
        }

        return response;
    }

    /**
     * /get-by-damage id --> Return the users having the passed damage type.
     *
     * @param id the id of damage
     * @return the db user info for specific type of damage or error message.
     */
    @RequestMapping("/user/damage")
    @ResponseBody
    public String damage(Long id) {
        logger.log(Level.INFO, "Get users with type of damage id:{0}", id);

        String response = null;

        try {
            if (id != null) {
                ObjectMapper mapper = new ObjectMapper();
                Damage damage = damageDao.findOne(id);

                if (damage != null) {
                    List<UserDepartment> uds = userDepartmentDao.findByDepartment(damage.getDepartment());

                    if (uds != null && !uds.isEmpty()) {
                        Long damageType = damage.getType();
                        Integer type = (damageType.equals(CauseTypeEnum.ELECRTICAL.getId())
                                ? UserTypeEnum.ELECTRICIAN.getId()
                                : damageType.equals(CauseTypeEnum.MECHANICAL.getId()) ? UserTypeEnum.ENGINEER.getId() : null);
                        List<String> ids = new ArrayList<>();

                        uds.stream().forEach((ud) -> {
                            ids.add(ud.getUsers());
                        });

                        if (type != null) {
                            List users = userDao.findByIdInAndTypeOrderByNameAsc(ids, type);
                            ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, User.class));

                            if (users != null && !users.isEmpty()) {
                                response = typedWriter.writeValueAsString(users);
                            } else {
                                response = "No found user with type id:" + id;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "User not found with error:" + ex.getMessage();
        }

        return response;
    }

    /**
     * /get-all --> Return the users.
     *
     * @return the db user info or error message.
     */
    @RequestMapping("/user/all")
    @ResponseBody
    public String all() {
        logger.log(Level.INFO, "Get all users");

        String response;

        try {
            ObjectMapper mapper = new ObjectMapper();
            Collection<User> users = (Collection<User>) userDao.findAll(new Sort(Sort.Direction.ASC, "name"));
            ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, User.class));

            if (users != null && !users.isEmpty()) {
                response = mapper.writeValueAsString(users);

            } else {
                response = "No found users";
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "User not found with error:" + ex.getMessage();
        }

        return response;
    }

    /**
     * /update --> Update the email and the name for the user in the database having the passed id.
     *
     * @param jsonUser The jsonUser of the user to be updated in database
     * @return A object if the user is succesfully updated (User object) or not (error message).
     */
    @RequestMapping("/user/update")
    @ResponseBody
    public String updateUser(String jsonUser) {
        logger.log(Level.INFO, "User update with user:{0}", jsonUser);

        String response;

        try {
            if (!Strings.isNullOrEmpty(jsonUser)) {
                ObjectMapper mapper = new ObjectMapper();
                User user = mapper.readValue(jsonUser, User.class);

                if (user != null) {
                    User dbuser = userDao.findOne(user.getId());

                    if (dbuser != null) {
                        dbuser.setChanges(user);
                        user = userDao.save(dbuser);
                        response = mapper.writeValueAsString(user);
                    } else {
                        response = "No found the user with id:" + user.getId();
                    }
                } else {
                    response = "No valided input info:" + jsonUser;
                }
            } else {
                response = "The input info was empty";
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Error updating the user: " + ex.getMessage();
        }

        return response;
    }

    // ------------------------
    // PRIVATE FIELDS
    // ------------------------
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserDepartmentDao userDepartmentDao;

    @Autowired
    private DamageDao damageDao;

    @Autowired
    private HttpServletRequest request;

} // class UserController
