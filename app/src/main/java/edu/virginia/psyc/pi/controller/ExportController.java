package edu.virginia.psyc.pi.controller;

import edu.virginia.psyc.mindtrails.domain.DoNotDelete;
import edu.virginia.psyc.mindtrails.domain.RestExceptions.NoSuchIdException;
import edu.virginia.psyc.mindtrails.domain.RestExceptions.NoSuchQuestionnaireException;
import edu.virginia.psyc.mindtrails.domain.RestExceptions.NotDeleteableException;
import edu.virginia.psyc.mindtrails.domain.questionnaire.QuestionnaireInfo;
import edu.virginia.psyc.mindtrails.domain.tracking.ExportLog;
import edu.virginia.psyc.mindtrails.persistence.ExportLogRepository;
import edu.virginia.psyc.mindtrails.persistence.ParticipantRepository;
import edu.virginia.psyc.mindtrails.service.ExportService;
import edu.virginia.psyc.pi.persistence.Questionnaire.QuestionnaireRepository;
import edu.virginia.psyc.pi.persistence.TrialDAO;
import edu.virginia.psyc.pi.persistence.TrialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a tool for Exporting data from the system and then
 * safely removing that data once it is on the remote system.
 *
 */
@Controller
@RequestMapping("/api/export")
public class ExportController  {

    private static final Logger LOG = LoggerFactory.getLogger(ExportController.class);

    @Autowired
    ParticipantRepository participantRepository;
    @Autowired
    ExportLogRepository exportLogRepository;
    @Autowired ExportService exportService;

    @RequestMapping(method= RequestMethod.GET)
    public @ResponseBody List<QuestionnaireInfo> list() {
        List<QuestionnaireInfo> infoList = exportService.listRepositories();
        int sum = 0;
        for(QuestionnaireInfo i : infoList) sum += i.getSize();
        exportLogRepository.save(new ExportLog(sum));
        return (infoList);
    }

    @RequestMapping(value="{name}", method= RequestMethod.GET)
    public @ResponseBody List<Object> listData(@PathVariable String name, @
                RequestParam(value = "greaterThan", required = false, defaultValue = "0") long id) {
        JpaRepository rep = exportService.getRepositoryForName(name);
        if (rep != null) {
            LOG.info("Found " + rep.count() + " items to return .");
            if (rep instanceof TrialRepository) {
                return(getTrialSummary((TrialRepository) rep));
            } else {
                if(id != 0 && rep instanceof QuestionnaireRepository) {
                    return ((QuestionnaireRepository) rep).findByIdGreaterThan(id);
                } else return rep.findAll();
            }
        }
        throw new NoSuchQuestionnaireException();
    }

    @RequestMapping(value="{name}/{id}", method=RequestMethod.DELETE)
    public @ResponseBody void delete(@PathVariable String name, @PathVariable long id) {
        Class<?> domainType = exportService.getDomainType(name);
        if (domainType != null) {
            if (domainType.isAnnotationPresent(DoNotDelete.class))
                throw new NotDeleteableException();
            JpaRepository rep = exportService.getRepositoryForName(name);
            try {
                rep.delete(id);
                rep.flush();
            } catch (EmptyResultDataAccessException e) {
                throw new NoSuchIdException();
            }
        } else {
            throw new NoSuchQuestionnaireException();
        }
    }



    /**
     * Returns the json data of a PIPlayer script, removing non-essential data so
     * the exporter can more easily handle it.
     * @return
     */
    public List<Object> getTrialSummary(TrialRepository trialRepository) {
        List<TrialDAO> trialData = trialRepository.findAll();
        List<Object> reports = new ArrayList<>();
        // Convert to trial summary
        for (TrialDAO data : trialData) {
            reports.add(data.toTrialJson().toInterpretationReport());
        }
        return reports;
    }


}


