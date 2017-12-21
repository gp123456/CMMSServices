/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.controllers;

import cmms.dao.CauseDao;
import cmms.dao.DamageDao;
import cmms.dao.SubcauseDao;
import cmms.models.Subcause;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
public class SubcauseController {

    private static final Logger logger = Logger.getLogger(SubcauseController.class.getName());

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    /**
     * all --> Return all sub causes of db in JSON format.
     *
     * @return the db sub causes info or error message.
     */
    @RequestMapping("/subcause/all")
    @ResponseBody
    public String all() {
        logger.log(Level.INFO, "Get all subcauses");

        String response;

        try {
            ObjectMapper mapper = new ObjectMapper();
            Collection<Subcause> types = (Collection<Subcause>) subcauseDao.findByEnableOrderByDescriptionAsc(true);
            ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Subcause.class));

            response = (types != null && !types.isEmpty())
                    ? typedWriter.writeValueAsString(types)
                    : "There weren't subcauses";
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all subcauses in db with error:" + ex.getMessage();
        }

        return response;
    }

    /**
     * list --> Return list sub-causes by cause id(s) of db in JSON format.
     *
     * @param id the id(s) of cause
     * @return the list db causes info or error message.
     */
    @RequestMapping("/subcause/causes")
    @ResponseBody
    public String causes(String[] id) {
        logger.log(Level.INFO, "Get list subcauses by cause id:{0}", Arrays.toString(id));

        String response = null;

        try {
            if (id != null && id.length > 0) {
                List<Long> causes = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Collection.class, Subcause.class));

                for (String cause : id) {
                    
                    Long _cause = Long.parseLong(cause);
                    causes.add(_cause);
                    
                }

                Collection<Subcause> subcauses = subcauseDao.findByCauseInAndEnableOrderByDescriptionAsc(causes, true);

                response = (subcauses != null && !subcauses.isEmpty())
                        ? typedWriter.writeValueAsString(subcauses)
                        : "There weren't subcauses for cause id:" + Arrays.toString(id);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}", ex.getStackTrace());
            response = "Search for all subcauses in db with error:" + ex.getMessage();
        }

        return response;
    }

    /**
     * delete damage --> Return a success message for delete subcause.
     *
     * @param id the id of subcause
     * @return the success message or error message.
     */
    @RequestMapping("/subcause/delete")
    @ResponseBody
    public String delete(Long id) {
        String response = null;

        try {
            logger.log(Level.INFO, "Delete subcause with id:{0}", id);

            if (id != null) {
                Subcause sc = subcauseDao.findOne(id);

                if (sc != null) {
                    sc.setEnable(Boolean.FALSE);
                    sc = subcauseDao.saveAndFlush(sc);

                    if (sc.isEnable().equals(Boolean.FALSE)) {
                        response = "Success delete subcause with id:" + sc.getId();
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
    private SubcauseDao subcauseDao;

    @Autowired
    private CauseDao causeDao;

    @Autowired
    private DamageDao damageDao;
} // class SubcauseController
