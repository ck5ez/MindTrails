package edu.virginia.psyc.pi.controller;

import edu.virginia.psyc.pi.domain.CBMStudy;
import edu.virginia.psyc.pi.domain.Participant;
import edu.virginia.psyc.pi.persistence.ParticipantDAO;
import edu.virginia.psyc.pi.persistence.ParticipantRepository;
import edu.virginia.psyc.pi.persistence.Questionnaire.*;
import edu.virginia.psyc.pi.persistence.TaskLogDAO;
import edu.virginia.psyc.pi.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.mail.MessagingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: dan
 * Date: 3/26/14
 * Time: 10:04 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/questions")
public class QuestionController extends BaseController {


    private DASS21_ASRepository dass21_asRepository;
    private DASS21_DSRepository dass21_dsRepository;
    private QOLRepository qol_Repository;
    private AUDIT_Repository audit_Repository;
    private FollowUp_ChangeInTreatment_Repository followup_Repository;
    private ImpactAnxiousImagery_Repository impact_Repository;
    private MentalHealthHxTxRepository mh_Repository;
    private MultiUserExperienceRepository mue_Repository;
    private PilotUserExperienceRepository pue_Repository;
    private CredibilityRepository credibilityRepository;
    private DemographicRepository demographicRepository;
    private ImageryPrimeRepository imageryPrimeRepository;
    private StateAnxietyRepository stateAnxiety_Repository;
    private RR_Repository rr_repository;
    private CCRepository cc_repository;
    private OARepository oa_repository;
    private ReRuRepository reru_repository;
    private DDRepository dd_repository;
    private DD_FURepository dd_fu_repository;
//    private StateAnxietyPreRepository stateAnxietyPre_Repository;
    private StateAnxietyPostRepository stateAnxietyPost_Repository;
    private BBSIQRepository bbsiqRepository;
    private AnxietyTriggersRepository anxietyTriggersRepository;
    private static final Logger LOG = LoggerFactory.getLogger(QuestionController.class);
    private SUDSRepository sudsRepository;
    private VividRepository vividRepository;
    private ReasonsForEndingRepository reasonsForEndingRepository;
    private CIHSRepository cihsRepository;



    @Autowired
    private EmailService emailService;



    /**
     * Spring automatically configures this object.
     * You can modify the location of this database by editing the application.properties file.
     */
    @Autowired
    public QuestionController(ParticipantRepository participantRepository,
                              DASS21_ASRepository dass21_asRepository,
                              DASS21_DSRepository dass21_dsRepository,
                              QOLRepository qol_Repository,
                              ImpactAnxiousImagery_Repository impact_Repository,
                              MentalHealthHxTxRepository mh_Repository,
                              MultiUserExperienceRepository mue_Repository,
                              PilotUserExperienceRepository pue_Repository,
                              AUDIT_Repository audit_Repository,
                              CredibilityRepository credibilityRepository,
                              DemographicRepository demographicRepository,
                              ImageryPrimeRepository imageryPrimeRepository,
                              StateAnxietyRepository stateAnxiety_Repository,
                              RR_Repository rr_repository,
                              CCRepository cc_repository,
                              OARepository oa_repository,
                              ReRuRepository reru_repository,
//                              StateAnxietyPreRepository stateAnxietyPre_Repository,
                              FollowUp_ChangeInTreatment_Repository followup_Repository,
                              StateAnxietyPostRepository stateAnxietyPost_Repository,
                              DDRepository dd_Repository,
                              DD_FURepository dd_fu_repository,
                              BBSIQRepository bbsiqRepository,
                              AnxietyTriggersRepository anxietyTriggersRepository,
                              SUDSRepository sudsRepository,
                              VividRepository vividRepository,
                              ReasonsForEndingRepository reasonsForEndingRepository) {
        this.participantRepository = participantRepository;
        this.dass21_asRepository = dass21_asRepository;
        this.credibilityRepository = credibilityRepository;
        this.demographicRepository = demographicRepository;
        this.audit_Repository = audit_Repository;
        this.dass21_dsRepository = dass21_dsRepository;
        this.qol_Repository = qol_Repository;
        this.impact_Repository = impact_Repository;
        this.mh_Repository = mh_Repository;
        this.mue_Repository = mue_Repository;
        this.pue_Repository = pue_Repository;
        this.imageryPrimeRepository = imageryPrimeRepository;
        this.stateAnxiety_Repository = stateAnxiety_Repository;
        this.rr_repository = rr_repository;
        this.cc_repository = cc_repository;
        this.oa_repository = oa_repository;
        this.reru_repository = reru_repository;
//        this.stateAnxietyPre_Repository = stateAnxietyPre_Repository;
        this.stateAnxietyPost_Repository = stateAnxietyPost_Repository;
        this.followup_Repository = followup_Repository;
        this.dd_repository = dd_Repository;
        this.dd_fu_repository = dd_fu_repository;
        this.bbsiqRepository = bbsiqRepository;
        this.anxietyTriggersRepository = anxietyTriggersRepository;
        this.sudsRepository = sudsRepository;
        this.vividRepository = vividRepository;
        this.reasonsForEndingRepository = reasonsForEndingRepository;
        this.cihsRepository = cihsRepository;
    }

    /**
     * Does some tasks common to all forms:
     * - Adds the current CBMStudy.NAME to the data being recorded
     * - Marks this "task" as complete, and moves the participant on to the next session
     * - Connects the data to the participant who completed it.
     *
     * @param data
     */
    private void recordSessionProgress(QuestionnaireData data) {

        ParticipantDAO dao = (ParticipantDAO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        dao = participantRepository.findByEmail(dao.getEmail()); // Refresh session object from database.
        Participant participant = participantRepository.entityToDomain(dao);

        // Record the session for which this questionnaire was completed.
        data.setSession(participant.getStudy().getCurrentSession().getName());

        // Log the completion of the task
        TaskLogDAO taskDao = new TaskLogDAO(dao, participant.getStudy().getCurrentSession().getName(),
                                            participant.getStudy().getCurrentSession().getCurrentTask().getName());
        dao.addTaskLog(taskDao);

        // Update the participant's session status, and save back to the database.
        participant.getStudy().completeCurrentTask();
        participantRepository.domainToEntity(participant, dao);
        participantRepository.save(dao);

        // Connect the participant to the data being recorded.
        data.setParticipantDAO(dao);
        data.setDate(new Date());
    }

    private ModelAndView modelAndView(Principal principal, String url, String name, Object model) {
           Map<String,Object> models = new HashMap<>();
           models.put("participant", getParticipant(principal));
           models.put(name, model);
           return new ModelAndView(url, models);
    }

    /**
     * DASS 21 AS
     * ---------*
     */
    @RequestMapping(value = "DASS21_AS", method = RequestMethod.GET)
    public ModelAndView showDASS21_AS(Principal principal) {
        return modelAndView(principal, "/questions/DASS21_AS", "DASS21_AS", new DASS21_AS());
    }

    @RequestMapping(value = "DASS21_AS", method = RequestMethod.POST)
    RedirectView handleDASS21_AS(@ModelAttribute("DASS21_AS") DASS21_AS dass21_as,
                                 BindingResult result, Principal principal) throws MessagingException {

        recordSessionProgress(dass21_as);
        dass21_asRepository.save(dass21_as);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "DASS21_AS/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportDASS21_AS() {
        return(objectListToCSV(dass21_asRepository.findAll()));
    }


    /**
     * DASS 21 DS
     * ---------*
     */
    @RequestMapping(value = "DASS21_DS", method = RequestMethod.GET)
    public ModelAndView showDASS21_DS(ModelMap model, Principal principal) {
        Participant p = getParticipant(principal);
        model.addAttribute("inSessions", p.inSession());
        model.addAttribute("p", p);
        return modelAndView(principal, "/questions/DASS21_DS", "DASS21_DS", new DASS21_DS());
    }

    @RequestMapping(value = "DASS21_DS", method = RequestMethod.POST)
    RedirectView handleDASS21_DS(@ModelAttribute("DASS21_DS") DASS21_DS dass21_ds,
                                 BindingResult result) {

        recordSessionProgress(dass21_ds);
        dass21_dsRepository.save(dass21_ds);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "DASS21_DS/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportDASS21_DS() {
        return(objectListToCSV(dass21_dsRepository.findAll()));
    }



    /**
     * QOL
     * ---------*
     */
    @RequestMapping(value = "QOL", method = RequestMethod.GET)
    public ModelAndView showqol(Principal principal) {
        return modelAndView(principal, "questions/QOL", "QOL", new QOL());
    }

    @RequestMapping(value = "QOL", method = RequestMethod.POST)
    RedirectView handleqol(@ModelAttribute("qol") QOL qol,
                           BindingResult result) {

        recordSessionProgress(qol);
        qol_Repository.save(qol);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "QOL/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportQOL() {
        return(objectListToCSV(qol_Repository.findAll()));
    }


    /**
     * AUDIT
     * ---------*
     */
    @RequestMapping(value = "audit", method = RequestMethod.GET)
    public ModelAndView showaudit(Principal principal) {
        return modelAndView(principal, "/questions/audit", "aduit", new AUDIT());
    }

    @RequestMapping(value = "aduit", method = RequestMethod.POST)
    RedirectView handleaudit(@ModelAttribute("audit") AUDIT audit,
                             BindingResult result) {

        recordSessionProgress(audit);
        audit_Repository.save(audit);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "audit/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportAudit() {
        return(objectListToCSV(audit_Repository.findAll()));
    }


    /**
     * Credibility
     * ---------*
     */
    @RequestMapping(value = "/credibility", method = RequestMethod.GET)
    public ModelAndView showCredibility(Principal principal) {
        return modelAndView(principal, "/questions/credibility", "credibility", new Credibility());
    }

    @RequestMapping(value = "/credibility", method = RequestMethod.POST)
    RedirectView handleCredibility(@ModelAttribute("credibility") Credibility credibility,
                                   BindingResult result) {

        recordSessionProgress(credibility);
        credibilityRepository.save(credibility);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "credibility/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportCredibility() {
        return(objectListToCSV(credibilityRepository.findAll()));
    }


    /**
     * FollowUp_ChangeInTreatment
     * ---------*
     */

    @RequestMapping(value = "FU", method = RequestMethod.GET)
    public ModelAndView showFollowUp(Principal principal) {
        return modelAndView(principal, "/questions/FU", "FU", new FollowUp_ChangeInTreatment());
    }

    @RequestMapping(value = "FU", method = RequestMethod.POST)
    RedirectView handleFollowUp(@ModelAttribute("FU") FollowUp_ChangeInTreatment followup,
                                BindingResult result) {

        recordSessionProgress(followup);
        followup_Repository.save(followup);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "FU/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportFu() {
        return(objectListToCSV(followup_Repository.findAll()));
    }

    /**
     * MentalHealthHxTx
     * ---------*
     */

    @RequestMapping(value = "MH", method = RequestMethod.GET)
    public ModelAndView showMentalHealthHxTx(Principal principal) {
        return modelAndView(principal, "/questions/MH", "MH", new MentalHealthHxTx());
    }

    @RequestMapping(value = "MH", method = RequestMethod.POST)
    RedirectView handleMentalHealthHxTx(@ModelAttribute("MH") MentalHealthHxTx mh,
                                        BindingResult result) {

        recordSessionProgress(mh);
        mh_Repository.save(mh);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "MH/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportMentalHealthHxTx() {
        return(objectListToCSV(mh_Repository.findAll()));
    }

    /**
     * Change in Help Seeking
     * -------------*
     */

    @RequestMapping(value="CIHS", method = RequestMethod.GET)
    public  ModelAndView showCIHS (Principal principal) {
        return modelAndView(principal, "/questions/CIHS", "CIHS", new CIHS());
    }

    @RequestMapping(value = "CIHS", method = RequestMethod.POST)
    RedirectView handleCIHS(@ModelAttribute("CIHS") CIHS cihs,
                            BindingResult result) {
        recordSessionProgress(cihs);
        cihsRepository.save(cihs);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "CIHS/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.y
    String exportCIHS() {return(objectListToCSV(cihsRepository.findAll())); }

    /**
     * MultiUserExperience
     * ---------*
     */

    @RequestMapping(value = "MUE", method = RequestMethod.GET)
    public ModelAndView showMultiUserExperience(Principal principal) {
        return modelAndView(principal, "/questions/MUE", "MUE", new MultiUserExperience());
    }

    @RequestMapping(value = "MUE", method = RequestMethod.POST)
    RedirectView handleMultiUserExperience(@ModelAttribute("MUE") MultiUserExperience mue,
                                           BindingResult result) {

        recordSessionProgress(mue);
        mue_Repository.save(mue);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "MUE/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportMUE() {
        return(objectListToCSV(mue_Repository.findAll()));
    }


    /**
     * PilotUserExperience
     * ---------*
     */

    @RequestMapping(value = "PUE", method = RequestMethod.GET)
    public ModelAndView showPilotUserExperience(Principal principal) {
        return modelAndView(principal, "questions/PUE", "PUE", new PilotUserExperience());
    }

    @RequestMapping(value = "PUE", method = RequestMethod.POST)
    RedirectView handlePilotUserExperience(@ModelAttribute("PUE") PilotUserExperience pue,
                                           BindingResult result) {

        recordSessionProgress(pue);
        pue_Repository.save(pue);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "PUE/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportPUE() {
        return(objectListToCSV(pue_Repository.findAll()));
    }

/**
 * SUDS
 * -----*
 *
 */
@RequestMapping(value = "SUDS", method = RequestMethod.GET)
public ModelAndView showSUDS(Principal principal) {
    return modelAndView(principal, "questions/SUDS", "SUDS", new SUDS());
}

    @RequestMapping(value = "SUDS", method = RequestMethod.POST)
    RedirectView handleSUDS(@ModelAttribute("SUDS") SUDS suds,
                                           BindingResult result) {

        recordSessionProgress(suds);
        sudsRepository.save(suds);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "SUDS/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportSUDS() {
        return(objectListToCSV(sudsRepository.findAll()));
    }

    /**
     * Vividness
     * -----*
     *
     */
    @RequestMapping(value = "Vivid", method = RequestMethod.GET)
    public ModelAndView showVivid(Principal principal) {
        return modelAndView(principal, "questions/Vivid", "Vivid", new Vivid());
    }

    @RequestMapping(value = "Vivid", method = RequestMethod.POST)
    RedirectView handleVivid(@ModelAttribute("Vivid") Vivid vivid,
                            BindingResult result) {

        recordSessionProgress(vivid);
        vividRepository.save(vivid);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "Vivid/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportVivid() {
        return(objectListToCSV(vividRepository.findAll()));
    }

    /**
     * ImpactAnxiousImagery
     * ---------*
     */

    @RequestMapping(value = "Impact", method = RequestMethod.GET)
    public ModelAndView showImpact(Principal principal) {
        return modelAndView(principal, "/questions/Impact", "Impact", new ImpactAnxiousImagery());
    }

    @RequestMapping(value = "Impact", method = RequestMethod.POST)
    RedirectView handleImpact(@ModelAttribute("Impact") ImpactAnxiousImagery impact,
                              BindingResult result) {

        recordSessionProgress(impact);
        impact_Repository.save(impact);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "Impact/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportImplact() {
        return(objectListToCSV(impact_Repository.findAll()));
    }


    /**
     * ImageryPrime
     * This can be either an AIP (Anxious Imagery Prime) or NIP (Neutral Imagery Prime) depending
     * on the status of the participant.
     * ---------*
     */

    @RequestMapping(value = "ImageryPrime", method = RequestMethod.GET)
    public ModelAndView showIP(ModelMap model, Principal principal) {
        Participant p = getParticipant(principal);
        boolean notFirst = p.getStudy().getCurrentSession().getIndex() > 1;
        model.addAttribute("notFirst", notFirst);
        model.addAttribute("prime", p.getPrime().toString());
        return modelAndView(principal, "/questions/ImageryPrime", "IP", new ImageryPrime());
    }

    @RequestMapping(value = "ImageryPrime", method = RequestMethod.POST)
    RedirectView handleIP(@ModelAttribute("IP") ImageryPrime prime,
                              BindingResult result) {
        recordSessionProgress(prime);
        imageryPrimeRepository.save(prime);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "ImageryPrime/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportIP() {
        return(objectListToCSV(imageryPrimeRepository.findAll()));
    }

    /**
     * Demographics
     * ---------*
     */
    @RequestMapping(value = "demographics", method = RequestMethod.GET)
    public ModelAndView showDemographics(Principal principal) {
        return modelAndView(principal, "/questions/demographics", "demographics", new Demographic());
    }

    @RequestMapping(value = "demographics", method = RequestMethod.POST)
    RedirectView handleDemographics(@ModelAttribute("demographics") Demographic demographic,
                                    BindingResult result) {

        recordSessionProgress(demographic);
        demographicRepository.save(demographic);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "demographics/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportDemographics() {
        return(objectListToCSV(demographicRepository.findAll()));
    }


    /**
     * StateAnxietyRepository
     * ---------*
     */
    @RequestMapping(value = "SA", method = RequestMethod.GET)
    public ModelAndView showSAP(Principal principal) {
        return modelAndView(principal, "/questions/SA", "SA", new StateAnxiety());
    }

    @RequestMapping(value = "SA", method = RequestMethod.POST)
    RedirectView handleSAP(@ModelAttribute("SA") StateAnxiety sa,
                              BindingResult result) {

        recordSessionProgress(sa);
        stateAnxiety_Repository.save(sa);
        return new RedirectView("/session/next");

    }

    @RequestMapping(value = "SA/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportSAP() {
        return(objectListToCSV(stateAnxiety_Repository.findAll()));
    }


    /**
     * RR
     * ---------*
     */
    @RequestMapping(value = "RR", method = RequestMethod.GET)
    public ModelAndView showRR(Principal principal) {
        return modelAndView(principal, "/questions/RR", "RR", new RR());
    }

    @RequestMapping(value = "RR", method = RequestMethod.POST)
    RedirectView handleRR(@ModelAttribute("RR") RR rr,
                                 BindingResult result) {

        recordSessionProgress(rr);
        rr_repository.save(rr);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "RR/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportRR() {
        return(objectListToCSV(rr_repository.findAll()));
    }


    /**
     * CC
     * ---------*
     */
    @RequestMapping(value = "CC", method = RequestMethod.GET)
    public ModelAndView showCC(Principal principal) {
        return modelAndView(principal, "/questions/CC", "CC", new CC());
    }

    @RequestMapping(value = "CC", method = RequestMethod.POST)
    RedirectView handleRR(@ModelAttribute("CC") CC cc,
                          BindingResult result) {

        recordSessionProgress(cc);
        cc_repository.save(cc);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "CC/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportCC() {
        return(objectListToCSV(cc_repository.findAll()));
    }


    /**
     * OA
     * ---------*
     */
    @RequestMapping(value = "OA", method = RequestMethod.GET)
    public ModelAndView showOA(ModelMap model, Principal principal) {
        Participant p = getParticipant(principal);
        model.addAttribute("inSessions", p.inSession());
        return modelAndView(principal, "/questions/OA", "OA", new OA());
    }

    @RequestMapping(value = "OA", method = RequestMethod.POST)
    RedirectView handleOA(@ModelAttribute("OA") OA oa,
                          BindingResult result, Principal principal) throws MessagingException{

        recordSessionProgress(oa);
        oa_repository.save(oa);

        // If the users score differs from there original score and places the user
        // "at-risk", then send a message to the administrator.
        List<OA> previous = oa_repository.findByParticipantDAO(oa.getParticipantDAO());
        OA firstEntry = Collections.min(previous, new Comparator<OA>() {
            public int compare(OA x, OA y) {
                return x.getDate().compareTo(y.getDate());
            }
        });
        if(oa.atRisk(firstEntry)) {
            Participant participant = getParticipant(principal);
            if (!participant.previouslySent(EmailService.TYPE.alertParticipant)) {
                emailService.sendAtRiskAdminEmail(participant, firstEntry, oa);
                emailService.sendSimpleMail(participant, EmailService.TYPE.alertParticipant);
            } else {
                LOG.info("User #" + participant.getId() + " continues to score poorly on assessment, but we've already notified everyone.");
            }
        }
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "OA/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportOA() {
        return(objectListToCSV(oa_repository.findAll()));
    }


    /**
     * ReRu
     * ---------*
     */
    @RequestMapping(value = "ReRu", method = RequestMethod.GET)
    public ModelAndView showReRu(Principal principal) {
        return modelAndView(principal, "/questions/ReRu", "ReRu", new ReRu());
    }

    @RequestMapping(value = "ReRu", method = RequestMethod.POST)
    RedirectView handleRR(@ModelAttribute("ReRu") ReRu reru,
                          BindingResult result) {

        recordSessionProgress(reru);
        reru_repository.save(reru);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "ReRu/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportReRu() {
        return(objectListToCSV(reru_repository.findAll()));
    }


    /**
     * Daily Drinking
     * ---------*
     */
    @RequestMapping(value = "DD", method = RequestMethod.GET)
    public ModelAndView showDD(Principal principal) {
        return modelAndView(principal, "/questions/DD", "DD", new DD());
    }

    @RequestMapping(value = "DD", method = RequestMethod.POST)
    RedirectView handleDD(@ModelAttribute("DD") DD dd,
                          BindingResult result) {

        recordSessionProgress(dd);
        dd_repository.save(dd);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "DD/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportDD() {
        return(objectListToCSV(dd_repository.findAll()));
    }

    /**
     * Daily Drinking Follow up
     * ---------*
     */
    @RequestMapping(value = "DD_FU", method = RequestMethod.GET)
    public ModelAndView showDDFU(Principal principal) {
        return modelAndView(principal, "/questions/DD_FU", "DD_FU", new DD());
    }

    @RequestMapping(value = "DD_FU", method = RequestMethod.POST)
    RedirectView handleDD(@ModelAttribute("DD_FU") DD_FU dd_fu,
                          BindingResult result) {

        recordSessionProgress(dd_fu);
        dd_fu_repository.save(dd_fu);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "DD_FU/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportDDFU() {
        return(objectListToCSV(dd_fu_repository.findAll()));
    }


    /**
     * BBSIQ
     * ---------*
     */
    @RequestMapping(value = "BBSIQ", method = RequestMethod.GET)
    public ModelAndView showBBSIQ(Principal principal) {
        return modelAndView(principal, "/questions/BBSIQ", "BBSIQ", new BBSIQ());
    }

    @RequestMapping(value = "BBSIQ", method = RequestMethod.POST)
    RedirectView handleDD(@ModelAttribute("BBSIQ") BBSIQ bbsiq,
                          BindingResult result) {

        recordSessionProgress(bbsiq);
        bbsiqRepository.save(bbsiq);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "BBSIQ/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportBBSIQ() {
        return(objectListToCSV(bbsiqRepository.findAll()));
    }


//    /**
//     * AnxietyPreRepository
//     * ---------*
//     */
//    @RequestMapping(value = "SAPr", method = RequestMethod.GET)
//    public ModelAndView showSAPr() {
//        return modelAndView(principal, "questions/SAPr", "SAPr", new StateAnxiety());
//    }
//
//    @RequestMapping(value = "SAPr", method = RequestMethod.POST)
//    RedirectView handleSAPr(@ModelAttribute("SAPr") StateAnxietyPre SAPr,
//                              BindingResult result) {
//
//        recordSessionProgress(SAPr);
//        stateAnxietyPre_Repository.save(SAPr);
//        return new RedirectView("/session/next");
//
//    }

    /**
     * StateAnxietypostRepository
     * ---------*
     */
    @RequestMapping(value = "SAPo", method = RequestMethod.GET)
    public ModelAndView showSAPo(Principal principal) {
        return modelAndView(principal, "/questions/SAPo", "SAPo", new StateAnxiety());
    }

    @RequestMapping(value = "SAPo", method = RequestMethod.POST)
    RedirectView handleSAPo(@ModelAttribute("SAPo") StateAnxietyPost SAPost,
                              BindingResult result) {

        recordSessionProgress(SAPost);
        stateAnxietyPost_Repository.save(SAPost);
        return new RedirectView("/session/next");

    }

    @RequestMapping(value = "SAPo/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportSAPo() {
        return(objectListToCSV(stateAnxietyPost_Repository.findAll()));
    }


    /**
     * Anxiety Triggers
     * ---------*
     */
    @RequestMapping(value = "AnxietyTriggers", method = RequestMethod.GET)
    public ModelAndView showAnxietyTriggers(Principal principal) {
        return modelAndView(principal, "/questions/AnxietyTriggers", "AnxietyTriggers", new AnxietyTriggers());
    }

    @RequestMapping(value = "AnxietyTriggers", method = RequestMethod.POST)
    RedirectView handleAnxietyTriggers(@ModelAttribute("AnxietyTriggers") AnxietyTriggers triggers,
                            BindingResult result) {

        recordSessionProgress(triggers);
        anxietyTriggersRepository.save(triggers);
        return new RedirectView("/session/next");
    }

    @RequestMapping(value = "AnxietyTriggers/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportAnxietyTriggers() {
        return(objectListToCSV(anxietyTriggersRepository.findAll()));
    }


    /**
     * Reasons For Ending Study
     * ---------*
     */
    @RequestMapping(value = "ReasonsForEnding", method = RequestMethod.GET)
    public ModelAndView showReasonsForEnding(Principal principal) {
        return modelAndView(principal, "/questions/ReasonsForEnding", "ReasonsForEnding", new ReasonsForEnding());
    }

    @RequestMapping(value = "ReasonsForEnding", method = RequestMethod.POST)
    String handleReasonsForEnding(@ModelAttribute("ReasonsForEnding") ReasonsForEnding reasons,
                                       BindingResult result) {

        recordSessionProgress(reasons);
        reasonsForEndingRepository.save(reasons);
        return "debriefing";
    }

    @RequestMapping(value = "ReasonsForEnding/export", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody // Return the string directly, the return value is not a template name.
    String exportReasonsForEnding() {
        return(objectListToCSV(reasonsForEndingRepository.findAll()));
    }



    /** ==============================================================
     *       Some utilility methods for exporting csv data from the forms
     *  ==============================================================
     */

    /**
     * Converts a list of objects into a string suitable for returning
     * as a csv.
     * @param objects
     * @return
     */
    public static String objectListToCSV(List objects) {
        StringBuffer csv = new StringBuffer();

        if(objects.size() < 1) return "";

        Method[] methods = objects.get(0).getClass().getMethods();

        // Add in headers.
        for(Method method : methods){
            if(isGetter(method)) {
                csv.append("\"");
                csv.append(method.getName().substring(3));
                csv.append("\"");
                csv.append(",");
            }
        }
        csv.append("\n");
        for(Object o : objects) {
            appendObjectToCSV(o, csv);
            csv.append("\n");
        }
        return csv.toString();
    }

    private static void appendObjectToCSV(Object o, StringBuffer csv) {
        Method[] methods = o.getClass().getMethods();
        ParticipantDAO participantDAO;
        CBMStudy.NAME session;
        List list;
        String data;

        for(Method method : methods){
            if(isGetter(method)) {
                try {
                    if(null == method.invoke(o)) {
                        data = "";
                    } else if(method.getReturnType().isPrimitive()) {
                        data = method.invoke(o).toString();
                    } else if (String.class.equals(method.getReturnType())) {
                        data = method.invoke(o).toString();
                    } else if (List.class.equals(method.getReturnType())) {
                        StringBuffer values = new StringBuffer();
                        list = (List)method.invoke(o);
                        for(int i = 0; i < list.size(); i++) {
                            values.append(list.get(i).toString());
                            if (i < list.size() -1) values.append("; ");
                        }
                        data = values.toString();
                    } else if (Date.class.equals(method.getReturnType())) {
                        data = method.invoke(o).toString();
                    } else if (ParticipantDAO.class.equals(method.getReturnType())) {
                        participantDAO = (ParticipantDAO)method.invoke(o);
                        data = "" + participantDAO.getId();
                    } else if (CBMStudy.NAME.class.equals(method.getReturnType())) {
                        session  = (CBMStudy.NAME)method.invoke(o);
                        data = session.toString();
                    } else if (Class.class.equals(method.getReturnType())) {
                        data = ((Class)method.invoke(o)).getSimpleName();
                    } else {
                        data = method.getReturnType().getName();
                    }
                    csv.append("\"");
                    csv.append(data.replaceAll("\"", "\\\""));
                    csv.append("\"");
                    csv.append(",");
                } catch (InvocationTargetException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IllegalAccessException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    public static boolean isGetter(Method method){
        if(!method.getName().startsWith("get"))       return false;
        if(method.getParameterTypes().length != 0)    return false;
        if(void.class.equals(method.getReturnType())) return false;
        return true;
    }




}

