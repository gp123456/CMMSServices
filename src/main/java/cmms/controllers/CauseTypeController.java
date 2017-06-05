/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.controllers;

import cmms.dao.CauseTypeDao;
import cmms.models.CauseType;
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
public class CauseTypeController {

    private static final Logger logger = Logger.getLogger(CauseTypeController.class.getName());

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    /**
     * all --> Return all cause types of db in JSON format.
     *
     * @return the db cause types info or error message.
     */
    @RequestMapping("/causetype/all")
    @ResponseBody
    public String all() {
        logger.log(Level.INFO, "Get all cause types");

        String response;

        try {
            ObjectMapper mapper = new ObjectMapper();
            Collection<CauseType> types = (Collection<CauseType>) causeTypeDao.findAll(new Sort(Sort.Direction.ASC, "Name"));
            ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, CauseType.class));

            response = (types != null && !types.isEmpty())
                    ? typedWriter.writeValueAsString(types)
                    : "There weren't cause types";
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all cause types in db with error:" + ex.getMessage();
        }

        return response;
    }

    @RequestMapping("/causetype")
    @ResponseBody
    @SuppressWarnings("UseSpecificCatch")
    public String getById(String[] id) {
        logger.log(Level.INFO, "Get cause types by id:{0}", Arrays.toString(id));

        String response = null;

        try {
            if (id != null && id.length > 0) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, CauseType.class));
                List<Long> ct = new ArrayList<>();
                
                for (String causetype : id) {
                    Long _causetype = Long.parseLong(causetype);
                    ct.add(_causetype);
                }
                
                if (!ct.isEmpty()) {
                    @SuppressWarnings("UnusedAssignment")
                    Collection<CauseType> causetype = new ArrayList<>();
                    
                    causetype = causeTypeDao.findByIdInOrderByNameAsc(ct);
                    
                    response = (causetype != null && !causetype.isEmpty())
                        ? typedWriter.writeValueAsString(causetype)
                        : "There weren't causetype";
                }

                
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all cause types in db with error:" + ex.getMessage();
        }

        return response;
    }

    // ------------------------
    // PRIVATE FIELDS
    // ------------------------
    @Autowired
    private CauseTypeDao causeTypeDao;
} // class CauseTypeController
