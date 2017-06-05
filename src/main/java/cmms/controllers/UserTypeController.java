/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.controllers;

import cmms.dao.UserTypeDao;
import cmms.models.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author gpatitakis
 */
@Controller
public class UserTypeController {

    private static final Logger logger = Logger.getLogger(UserTypeController.class.getName());

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    /**
     * all --> Return all departments of db in json format.
     *
     * @return the db department info or error message.
     */
    @RequestMapping("/usertype/all")
    @ResponseBody
    public String all() {
        logger.log(Level.INFO, "Get all user types");

        String response;

        try {
            ObjectMapper mapper = new ObjectMapper();
            Collection<UserType> types = (Collection<UserType>) userTypeDao.findAll();
            ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, UserType.class));

            response = (types != null && !types.isEmpty())
                    ? typedWriter.writeValueAsString(types)
                    : "There weren't user types";
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all user types in db with error:" + ex.getMessage();
        }

        return response;
    }

    // ------------------------
    // PRIVATE FIELDS
    // ------------------------
    @Autowired
    private UserTypeDao userTypeDao;
} // class UserTypeController
