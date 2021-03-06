package edu.virginia.psyc.pi.persistence.Questionnaire;

import edu.virginia.psyc.pi.domain.Session;
import edu.virginia.psyc.pi.persistence.ParticipantDAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: dan
 * Date: 5/26/14
 * Time: 1:55 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name="Demographic")
@EqualsAndHashCode(callSuper = true)
@Data
public class Demographic extends QuestionnaireData {

    private String gender;
    private int   birthYear;
    private String race;
    private String ethnicity;
    private String residenceCountry;
    private String education;
    private String maritalStatus;
    private String employmentStatus;
    private String income;
    private String participateReason;
}